import type { AiAgentContextMode, SkillGapAnalysisResult } from '../types/skillbridge';
import { resolveApiUrl } from './ecosystemApi';

async function streamSseEndpoint(
  url: string,
  body: Record<string, unknown>,
  onChunk: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  const response = await fetch(resolveApiUrl(url), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body),
    signal,
  });

  if (!response.ok) {
    const errText = await response.text().catch(() => '');
    throw new Error(errText || `HTTP ${response.status} from AI service`);
  }

  if (!response.body) {
    throw new Error('Streaming response body is unavailable.');
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
  let buffer = '';

  while (true) {
    const { value, done } = await reader.read();
    if (done) break;
    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() ?? '';

    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed.startsWith('data:')) continue;
      const jsonStr = trimmed.slice('data:'.length).trim();
      if (!jsonStr) continue;
      let event: { delta?: string; error?: string; done?: boolean } | null = null;
      try {
        event = JSON.parse(jsonStr) as { delta?: string; error?: string; done?: boolean };
      } catch {
        continue;
      }
      if (event.error) {
        throw new Error(event.error);
      }
      if (event.delta) {
        onChunk(event.delta);
      }
      if (event.done) {
        return;
      }
    }
  }
}

export async function analyzeSkillGapApi(params: {
  targetRole: string;
  experienceLevel: string;
  currentSkills: string[];
  backgroundNotes: string;
  weeklyHours: number;
  isArabic: boolean;
}): Promise<SkillGapAnalysisResult> {
  const response = await fetch(resolveApiUrl('/api/ai/analyze-gap'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(params),
  });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || `Failed to analyze skill gap (HTTP ${response.status})`);
  }
  return data as SkillGapAnalysisResult;
}

export async function streamCoachReplyApi(
  params: {
    mode: 'COACH' | 'INTERVIEW';
    targetRole: string;
    matchedSkills: string[];
    topGaps: string[];
    history: Array<{ sender: string; content: string }>;
    userPrompt: string;
    isArabic: boolean;
  },
  onChunk: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  return streamSseEndpoint('/api/ai/coach', params, onChunk, signal);
}

export async function streamAgentReplyApi(
  params: {
    mode: AiAgentContextMode;
    isArabic: boolean;
    contextSummary: string;
    allowPersonalization: boolean;
    allowContextSharing: boolean;
    history: Array<{ sender: string; content: string }>;
    userPrompt: string;
  },
  onChunk: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  return streamSseEndpoint('/api/ai/agent', params, onChunk, signal);
}

export async function streamWorkSimulationApi(
  params: {
    challengeTitle: string;
    userDecision: string;
    isArabic: boolean;
  },
  onChunk: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  return streamSseEndpoint('/api/ai/work-sim', params, onChunk, signal);
}

export async function streamMilestoneTutorApi(
  params: {
    targetRole: string;
    milestoneTitle: string;
    milestoneDescription: string;
    practiceProject: string;
    isArabic: boolean;
  },
  onChunk: (delta: string) => void,
  signal?: AbortSignal,
): Promise<void> {
  return streamSseEndpoint('/api/ai/milestone-tutor', params, onChunk, signal);
}
