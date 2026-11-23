import express from 'express';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { GoogleGenAI, Type, ThinkingLevel } from '@google/genai';
import { CentralEcosystemStore, BackendHttpError } from './server/ecosystemStore.ts';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const isProd = process.env.NODE_ENV === 'production';
const PORT = isProd ? Number(process.env.PORT || 3000) : 3000;
const MODEL_NAME = 'gemini-4-hydrogen';

const ecosystemStore = new CentralEcosystemStore(path.join(__dirname, 'data'));

function getAiClient(): GoogleGenAI {
  const apiKey = process.env.GEMINI_API_KEY;
  if (!apiKey || !apiKey.trim()) {
    throw new Error('GEMINI_API_KEY is not configured in server environment Secrets.');
  }
  return new GoogleGenAI({
    apiKey: apiKey.trim(),
  });
}

function formatAiError(error: unknown): { status: number; message: string } {
  const raw = error instanceof Error ? error.message : String(error);
  if (raw.includes('GEMINI_API_KEY')) {
    return { status: 503, message: 'AI service unavailable: please configure GEMINI_API_KEY in server environment variables.' };
  }
  if (raw.includes('403') || raw.includes('PERMISSION_DENIED') || raw.includes('API_KEY_INVALID')) {
    return { status: 403, message: 'Gemini API key error (403/400). Please check your GEMINI_API_KEY configuration.' };
  }
  if (raw.includes('429') || raw.includes('RESOURCE_EXHAUSTED')) {
    return { status: 429, message: 'AI rate quota reached (429). Please retry shortly or check your API quota.' };
  }
  if (raw.includes('404') || raw.includes('NOT_FOUND')) {
    return { status: 404, message: `Model ${MODEL_NAME} returned NOT_FOUND. Verify model availability.` };
  }
  return { status: 500, message: raw || 'Upstream AI request failed.' };
}

function setupSse(res: express.Response) {
  res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
  res.setHeader('Cache-Control', 'no-cache, no-transform');
  res.setHeader('Connection', 'keep-alive');
}

function sendSseEvent(res: express.Response, payload: Record<string, unknown>) {
  res.write(`data: ${JSON.stringify(payload)}\n\n`);
}

function extractBearerSession(req: express.Request) {
  const authHeader = req.headers.authorization || '';
  if (!authHeader.startsWith('Bearer ')) {
    throw new BackendHttpError(401, 'Missing Authorization Bearer token.');
  }
  const token = authHeader.slice('Bearer '.length).trim();
  return ecosystemStore.verifyToken(token);
}

function handleBackendError(res: express.Response, error: unknown) {
  if (error instanceof BackendHttpError) {
    res.status(error.status).json({ error: error.message });
    return;
  }
  const msg = error instanceof Error ? error.message : 'Unexpected server error.';
  res.status(500).json({ error: msg });
}

async function startServer() {
  const app = express();
  app.use(express.json({ limit: '1mb' }));

  // Origin-allowlisted CORS for web deployments across Vercel, Netlify, GitHub Pages, or custom domains
  const extraOrigins = (process.env.ALLOWED_ORIGINS || process.env.CORS_ORIGIN || '')
    .split(',')
    .map((v) => v.trim())
    .filter(Boolean);
  const allowedOrigins = new Set(
    [
      process.env.APP_URL,
      process.env.SHARED_APP_URL,
      ...extraOrigins,
      'http://localhost:3000',
      'http://127.0.0.1:3000',
      'http://localhost:5173',
      'http://127.0.0.1:5173',
    ].filter((v): v is string => Boolean(v && v.trim())),
  );
  app.use('/api', (req, res, next) => {
    const origin = req.headers.origin;
    if (origin && allowedOrigins.has(origin)) {
      res.setHeader('Access-Control-Allow-Origin', origin);
      res.setHeader('Vary', 'Origin');
      res.setHeader('Access-Control-Allow-Headers', 'Authorization, Content-Type');
      res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
    }
    if (req.method === 'OPTIONS') {
      res.status(204).end();
      return;
    }
    next();
  });

  app.get('/api/health', (_req, res) => {
    res.json({ ok: true, service: 'skillbridge-web', model: MODEL_NAME });
  });

  // --- Central Ecosystem Auth & Synchronization APIs ---

  app.post('/api/auth/login', (req, res) => {
    try {
      const { loginId, password } = req.body ?? {};
      const result = ecosystemStore.authenticateWithCredentials(String(loginId ?? ''), String(password ?? ''));
      res.json(result);
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/auth/register', (req, res) => {
    try {
      const { loginId, displayName, email, password } = req.body ?? {};
      const result = ecosystemStore.registerStudentAccount({
        loginId: String(loginId ?? ''),
        displayName: String(displayName ?? ''),
        email: String(email ?? ''),
        password: String(password ?? ''),
      });
      res.status(201).json(result);
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/auth/google-session', (req, res) => {
    try {
      const { firebaseUid, displayName, email } = req.body ?? {};
      const result = ecosystemStore.resolveGoogleStudentSession({
        firebaseUid: String(firebaseUid ?? ''),
        displayName: String(displayName ?? ''),
        email: String(email ?? ''),
      });
      res.json(result);
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.get('/api/auth/me', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      res.json({ principal });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.get('/api/ecosystem/public', (_req, res) => {
    res.json(ecosystemStore.getPublicCatalogSummary());
  });

  app.get('/api/ecosystem/snapshot', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      res.json(ecosystemStore.getSnapshotForSession(principal));
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  // Real-time Server-Sent Events stream across Web, Mobile PWA & Desktop PWA
  app.get('/api/events', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      setupSse(res);
      sendSseEvent(res, { type: 'connected', accountId: principal.accountId, role: principal.role });

      const unsubscribe = ecosystemStore.subscribe((event) => {
        sendSseEvent(res, { type: 'sync', ...event });
      });

      const heartbeat = setInterval(() => {
        res.write(': keepalive\n\n');
      }, 25000);

      req.on('close', () => {
        clearInterval(heartbeat);
        unsubscribe();
      });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/device-session', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { sessionId, deviceType, deviceLabel, windowWidthDp } = req.body ?? {};
      const entity = ecosystemStore.registerDeviceSession(principal, {
        sessionId: String(sessionId ?? ''),
        deviceType: deviceType === 'DESKTOP_LAPTOP' ? 'DESKTOP_LAPTOP' : 'MOBILE',
        deviceLabel: String(deviceLabel ?? ''),
        windowWidthDp: Number(windowWidthDp ?? 1280),
      });
      res.json({ session: entity, snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/device-session/revoke', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { targetSessionId } = req.body ?? {};
      ecosystemStore.revokeDeviceSession(principal, String(targetSessionId ?? ''));
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/briefs', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const brief = ecosystemStore.createProjectBrief(principal, req.body ?? {});
      res.status(201).json({ brief, snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/briefs/:briefId/assign-director', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { directorAccountId, expectedUpdatedAtEpochMs } = req.body ?? {};
      ecosystemStore.assignDirectorToBrief(principal, req.params.briefId, String(directorAccountId ?? ''), expectedUpdatedAtEpochMs);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/briefs/:briefId/review', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { approve, adminNotes, expectedUpdatedAtEpochMs } = req.body ?? {};
      ecosystemStore.reviewProjectBrief(principal, req.params.briefId, Boolean(approve), String(adminNotes ?? ''), expectedUpdatedAtEpochMs);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/briefs/:briefId/complete', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      ecosystemStore.markBriefCompleted(principal, req.params.briefId);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/briefs/:briefId/apply', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { coverNote, roleRequested } = req.body ?? {};
      const application = ecosystemStore.applyToProjectBrief(principal, req.params.briefId, String(coverNote ?? ''), String(roleRequested ?? ''));
      res.status(201).json({ application, snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/applications/:applicationId/decide', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { newStatus, decisionNote, expectedUpdatedAtEpochMs } = req.body ?? {};
      ecosystemStore.decideApplication(principal, req.params.applicationId, newStatus, String(decisionNote ?? ''), expectedUpdatedAtEpochMs);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/tasks', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const task = ecosystemStore.createProjectTask(principal, req.body ?? {});
      res.status(201).json({ task, snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/tasks/:taskId/status', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { newStatus, expectedUpdatedAtEpochMs } = req.body ?? {};
      ecosystemStore.updateStudentTaskStatus(principal, req.params.taskId, newStatus, expectedUpdatedAtEpochMs);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/submissions', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const submission = ecosystemStore.submitProjectWork(principal, req.body ?? {});
      res.status(201).json({ submission, snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/submissions/:submissionId/evaluate', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      ecosystemStore.evaluateSubmission(principal, {
        submissionId: req.params.submissionId,
        ...(req.body ?? {}),
      });
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/companies/:companyId/verify', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { approve, tier, notes } = req.body ?? {};
      ecosystemStore.reviewCompanyVerification(principal, req.params.companyId, Boolean(approve), tier, String(notes ?? ''));
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/users/:accountId/active', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { active } = req.body ?? {};
      ecosystemStore.setUserActiveStatus(principal, req.params.accountId, Boolean(active));
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/reports', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { targetType, targetId, reason } = req.body ?? {};
      ecosystemStore.submitModerationReport(principal, String(targetType ?? 'BRIEF'), String(targetId ?? ''), String(reason ?? ''));
      res.status(201).json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/reports/:reportId/resolve', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      const { status, resolutionNote } = req.body ?? {};
      ecosystemStore.resolveModerationReport(principal, req.params.reportId, status, String(resolutionNote ?? ''));
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/profile', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      ecosystemStore.updateStudentProfile(principal, req.body ?? {});
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/notifications/read-all', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      ecosystemStore.markAllNotificationsRead(principal);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  app.post('/api/ecosystem/notifications/:notificationId/read', (req, res) => {
    try {
      const principal = extractBearerSession(req);
      ecosystemStore.markNotificationRead(principal, req.params.notificationId);
      res.json({ snapshot: ecosystemStore.getSnapshotForSession(principal) });
    } catch (error) {
      handleBackendError(res, error);
    }
  });

  // --- Server-Side Gemini 4 AI Endpoints ---

  // 1. Structured Skill Gap Analysis & Career Bridge Roadmap
  app.post('/api/ai/analyze-gap', async (req, res) => {
    try {
      const { targetRole, experienceLevel, currentSkills, backgroundNotes, weeklyHours, isArabic } = req.body ?? {};
      if (!targetRole || typeof targetRole !== 'string' || !targetRole.trim()) {
        res.status(400).json({ error: 'Target role is required.' });
        return;
      }

      const ai = getAiClient();
      const langInstruction = isArabic
        ? 'Respond strictly in clear, professional Arabic for all natural language descriptions, executiveSummary, whyItMatters, milestone titles, descriptions, practiceProject, and resourceTip.'
        : 'Respond strictly in clear, concise English.';

      const skillsList = Array.isArray(currentSkills) && currentSkills.length > 0
        ? currentSkills.join(', ')
        : 'None provided yet';

      const prompt = `
        Analyze a learner transitioning into a target career role on SkillBridge.
        Target Role: ${targetRole.trim()}
        Transition / Experience Level: ${experienceLevel || 'Junior -> Mid-level'}
        Weekly Study Commitment: ${Number(weeklyHours) || 10} hours/week
        Current Verified & Self-Reported Skills: ${skillsList}
        Background Notes: ${backgroundNotes || 'None'}

        Produce a realistic readinessScore (0-100), executiveSummary, matchedSkills, 4-6 prioritized skillGaps (HIGH/MEDIUM/LOW), and 4-5 sequential hands-on milestones ordered by phaseOrder.
      `.trim();

      const response = await ai.models.generateContent({
        model: MODEL_NAME,
        contents: prompt,
        config: {
          systemInstruction: `${langInstruction} Keep recommendations actionable, portfolio-focused, and realistic for the user's weekly study hours.`,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
          responseMimeType: 'application/json',
          responseSchema: {
            type: Type.OBJECT,
            properties: {
              readinessScore: { type: Type.INTEGER },
              executiveSummary: { type: Type.STRING },
              matchedSkills: {
                type: Type.ARRAY,
                items: { type: Type.STRING },
              },
              skillGaps: {
                type: Type.ARRAY,
                items: {
                  type: Type.OBJECT,
                  properties: {
                    skillName: { type: Type.STRING },
                    priority: { type: Type.STRING },
                    currentLevel: { type: Type.STRING },
                    targetLevel: { type: Type.STRING },
                    whyItMatters: { type: Type.STRING },
                  },
                  required: ['skillName', 'priority', 'currentLevel', 'targetLevel', 'whyItMatters'],
                },
              },
              milestones: {
                type: Type.ARRAY,
                items: {
                  type: Type.OBJECT,
                  properties: {
                    phaseOrder: { type: Type.INTEGER },
                    title: { type: Type.STRING },
                    description: { type: Type.STRING },
                    estimatedHours: { type: Type.INTEGER },
                    practiceProject: { type: Type.STRING },
                    resourceTip: { type: Type.STRING },
                  },
                  required: ['phaseOrder', 'title', 'description', 'estimatedHours', 'practiceProject', 'resourceTip'],
                },
              },
            },
            required: ['readinessScore', 'executiveSummary', 'matchedSkills', 'skillGaps', 'milestones'],
          },
        },
      });

      const rawText = (response.text ?? '{}').trim();
      const parsed = JSON.parse(rawText);
      res.json(parsed);
    } catch (error) {
      const formatted = formatAiError(error);
      res.status(formatted.status).json({ error: formatted.message });
    }
  });

  // 2. AI Career Coach & Mock Interview (SSE Streaming)
  app.post('/api/ai/coach', async (req, res) => {
    setupSse(res);
    try {
      const { mode, targetRole, matchedSkills, topGaps, history, userPrompt, isArabic } = req.body ?? {};
      if (!userPrompt || typeof userPrompt !== 'string' || !userPrompt.trim()) {
        sendSseEvent(res, { error: 'Prompt is required.' });
        res.end();
        return;
      }

      const ai = getAiClient();
      const isInterview = mode === 'INTERVIEW';
      const systemInstruction = isInterview
        ? `You are an expert technical interviewer on SkillBridge for the role: ${targetRole || 'Software Engineer'}. Ask crisp, practical questions one step at a time or evaluate the candidate's answer with constructive rubric scoring and 1 follow-up question. ${isArabic ? 'Reply in Arabic.' : 'Reply in English.'}`
        : `You are SkillBridge AI Career Coach. Target Role: ${targetRole || 'Software Engineer'}. Matched Skills: ${(matchedSkills || []).join(', ')}. Priority Gaps: ${(topGaps || []).join(', ')}. Give concise, practical guidance grounded in hands-on portfolio challenges. ${isArabic ? 'Reply in Arabic.' : 'Reply in English.'}`;

      const recentHistory = Array.isArray(history)
        ? history.slice(-8).map((m: { sender?: string; content?: string }) => `${m.sender === 'AI' ? 'Coach' : 'User'}: ${m.content}`).join('\n')
        : '';

      const prompt = recentHistory
        ? `Conversation so far:\n${recentHistory}\n\nUser: ${userPrompt.trim()}`
        : userPrompt.trim();

      const stream = await ai.models.generateContentStream({
        model: MODEL_NAME,
        contents: prompt,
        config: {
          systemInstruction,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
      });

      for await (const chunk of stream) {
        if (chunk.text) {
          sendSseEvent(res, { delta: chunk.text });
        }
      }
      sendSseEvent(res, { done: true });
      res.end();
    } catch (error) {
      const formatted = formatAiError(error);
      sendSseEvent(res, { error: formatted.message });
      res.end();
    }
  });

  // 3. Contextual AI Agent (6 modes, SSE Streaming)
  app.post('/api/ai/agent', async (req, res) => {
    setupSse(res);
    try {
      const { mode, isArabic, contextSummary, allowPersonalization, allowContextSharing, history, userPrompt } = req.body ?? {};
      if (!userPrompt || typeof userPrompt !== 'string' || !userPrompt.trim()) {
        sendSseEvent(res, { error: 'Prompt is required.' });
        res.end();
        return;
      }

      const ai = getAiClient();
      const contextBlock = allowPersonalization && allowContextSharing && contextSummary
        ? `Context:\n${contextSummary}`
        : 'Privacy settings restrict profile context sharing; provide general domain guidance only.';

      const systemInstruction = `
        You are SkillBridge Contextual AI Agent operating strictly in mode: ${mode || 'SKILL_ADVISOR'}.
        ${isArabic ? 'Respond strictly in clear, professional Arabic.' : 'Respond strictly in clear, concise English.'}
        Respect all safety and integrity boundaries:
        - Never solve graded contests or final challenge submissions outright.
        - Never fabricate verified badges, points, or credentials.
        - Never assist in unauthorized reverse engineering or credential attacks; focus strictly on authorized sandbox analysis & understanding reports.
        - Keep answers structured with crisp bullet points under 180 words.
        ${contextBlock}
      `.trim();

      const recentHistory = Array.isArray(history)
        ? history.slice(-6).map((m: { sender?: string; content?: string }) => `${m.sender === 'AI' ? 'Agent' : 'User'}: ${m.content}`).join('\n')
        : '';

      const fullPrompt = recentHistory
        ? `Recent chat:\n${recentHistory}\n\nLatest prompt: ${userPrompt.trim()}`
        : userPrompt.trim();

      const stream = await ai.models.generateContentStream({
        model: MODEL_NAME,
        contents: fullPrompt,
        config: {
          systemInstruction,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
      });

      for await (const chunk of stream) {
        if (chunk.text) {
          sendSseEvent(res, { delta: chunk.text });
        }
      }
      sendSseEvent(res, { done: true });
      res.end();
    } catch (error) {
      const formatted = formatAiError(error);
      sendSseEvent(res, { error: formatted.message });
      res.end();
    }
  });

  // 4. Practical Work Simulation Evaluator (SSE Streaming)
  app.post('/api/ai/work-sim', async (req, res) => {
    setupSse(res);
    try {
      const { challengeTitle, userDecision, isArabic } = req.body ?? {};
      if (!userDecision || typeof userDecision !== 'string' || !userDecision.trim()) {
        sendSseEvent(res, { error: 'Simulation decision is required.' });
        res.end();
        return;
      }

      const ai = getAiClient();
      const systemInstruction = `
        You are the Lead Systems Reviewer inside SkillBridge's Practical Work Simulation for "${challengeTitle || 'Engineering Sprint'}".
        ${isArabic ? 'Respond strictly in Arabic.' : 'Respond strictly in English.'}
        Evaluate the candidate's engineering decision against production reliability, edge cases, and rubric clarity. Return:
        1) Trade-off evaluation (2 bullets)
        2) One hidden edge case to test next
        3) Readiness verdict (Strong / Needs Refinement).
      `.trim();

      const stream = await ai.models.generateContentStream({
        model: MODEL_NAME,
        contents: `Candidate architectural decision:\n${userDecision.trim()}`,
        config: {
          systemInstruction,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
      });

      for await (const chunk of stream) {
        if (chunk.text) {
          sendSseEvent(res, { delta: chunk.text });
        }
      }
      sendSseEvent(res, { done: true });
      res.end();
    } catch (error) {
      const formatted = formatAiError(error);
      sendSseEvent(res, { error: formatted.message });
      res.end();
    }
  });

  // 5. Milestone Micro-Sprint Tutor (SSE Streaming)
  app.post('/api/ai/milestone-tutor', async (req, res) => {
    setupSse(res);
    try {
      const { targetRole, milestoneTitle, milestoneDescription, practiceProject, isArabic } = req.body ?? {};
      const ai = getAiClient();
      const systemInstruction = `
        You are SkillBridge Micro-Sprint Tutor helping a learner targeting ${targetRole || 'their career goal'}.
        ${isArabic ? 'Respond strictly in Arabic.' : 'Respond strictly in English.'}
        Provide a focused 3-step hands-on execution plan and 2 self-verification checks for the milestone. Keep under 160 words.
      `.trim();

      const prompt = `Milestone: ${milestoneTitle || ''}\nDescription: ${milestoneDescription || ''}\nPractice Project: ${practiceProject || ''}`;
      const stream = await ai.models.generateContentStream({
        model: MODEL_NAME,
        contents: prompt,
        config: {
          systemInstruction,
          thinkingConfig: { thinkingLevel: ThinkingLevel.LOW },
        },
      });

      for await (const chunk of stream) {
        if (chunk.text) {
          sendSseEvent(res, { delta: chunk.text });
        }
      }
      sendSseEvent(res, { done: true });
      res.end();
    } catch (error) {
      const formatted = formatAiError(error);
      sendSseEvent(res, { error: formatted.message });
      res.end();
    }
  });

  if (!isProd) {
    const { createServer: createViteServer } = await import('vite');
    const vite = await createViteServer({
      server: { middlewareMode: true, hmr: false, ws: false },
      appType: 'spa',
    });
    app.get('/@vite/client', async (_req, res, next) => {
      try {
        const result = await vite.transformRequest('/@vite/client');
        if (!result) return next();
        const patched = result.code
          .replace(
            'transport.connect(createHMRHandler(handleMessage));',
            '/* HMR disabled: skip WebSocket connection */ void 0;',
          )
          .replace(
            'else throw new Error("send was called before connect");',
            'else return;',
          )
          .replace(
            'else throw new Error("invoke was called before connect");',
            'else return;',
          );
        res.setHeader('Content-Type', 'application/javascript');
        res.send(patched);
      } catch (err) {
        next(err);
      }
    });
    app.use(vite.middlewares);
  } else {
    const distPath = path.join(__dirname, 'dist');
    app.use(express.static(distPath));
    app.get('/{*path}', (_req, res) => {
      res.sendFile(path.join(distPath, 'index.html'));
    });
  }

  app.listen(PORT, '0.0.0.0', () => {
    console.log(`SkillBridge server listening on http://0.0.0.0:${PORT}`);
  });
}

startServer().catch((err) => {
  console.error('Failed to start SkillBridge server:', err);
  process.exit(1);
});
