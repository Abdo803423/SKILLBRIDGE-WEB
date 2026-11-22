import type {
  AccountEntity,
  CompanyEntity,
  DeviceSessionEntity,
  ModerationReportEntity,
  PlatformActivityEntity,
  PlatformNotificationEntity,
  ProjectApplicationEntity,
  ProjectBriefEntity,
  ProjectTaskEntity,
  SessionPrincipal,
  WorkSubmissionEntity,
} from '../types/skillbridge';

export interface CentralEcosystemSnapshot {
  revision: number;
  updatedAtEpochMs: number;
  account: AccountEntity | null;
  accounts: AccountEntity[];
  companies: CompanyEntity[];
  briefs: ProjectBriefEntity[];
  applications: ProjectApplicationEntity[];
  tasks: ProjectTaskEntity[];
  submissions: WorkSubmissionEntity[];
  notifications: PlatformNotificationEntity[];
  activities: PlatformActivityEntity[];
  reports: ModerationReportEntity[];
  deviceSessions: DeviceSessionEntity[];
}

export interface PublicCatalogSummary {
  revision: number;
  updatedAtEpochMs: number;
  companies: CompanyEntity[];
  publishedBriefs: ProjectBriefEntity[];
}

export interface DownloadArtifactMetadata {
  id: 'android' | 'windows';
  available: boolean;
  filename: string;
  sizeBytes: number;
  sha256: string;
  updatedAtIso: string;
  downloadUrl: string;
}

export interface DownloadsStatusResponse {
  android: DownloadArtifactMetadata;
  windows: DownloadArtifactMetadata;
}

export async function fetchDownloadsStatusApi() {
  return apiJson<DownloadsStatusResponse>('/api/ecosystem/downloads/status');
}

async function apiJson<T>(url: string, options: RequestInit = {}, token?: string | null): Promise<T> {
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    ...(options.headers as Record<string, string> | undefined),
  };
  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }
  const response = await fetch(url, { ...options, headers });
  const data = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(data.error || `Request failed (${response.status})`);
  }
  return data as T;
}

export async function loginWithCredentialsApi(loginId: string, password: string) {
  return apiJson<{ token: string; principal: SessionPrincipal }>('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ loginId, password }),
  });
}

export async function registerStudentApi(params: {
  loginId: string;
  displayName: string;
  email: string;
  password: string;
}) {
  return apiJson<{ token: string; principal: SessionPrincipal }>('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(params),
  });
}

export async function resolveGoogleSessionApi(params: {
  firebaseUid: string;
  displayName: string;
  email: string;
}) {
  return apiJson<{ token: string; principal: SessionPrincipal }>('/api/auth/google-session', {
    method: 'POST',
    body: JSON.stringify(params),
  });
}

export async function fetchPublicCatalogApi() {
  return apiJson<PublicCatalogSummary>('/api/ecosystem/public');
}

export async function fetchEcosystemSnapshotApi(token: string) {
  return apiJson<CentralEcosystemSnapshot>('/api/ecosystem/snapshot', { method: 'GET' }, token);
}

export async function registerDeviceSessionApi(
  token: string,
  params: { sessionId: string; deviceType: 'MOBILE' | 'DESKTOP_LAPTOP'; deviceLabel: string; windowWidthDp: number },
) {
  return apiJson<{ session: DeviceSessionEntity; snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/device-session',
    { method: 'POST', body: JSON.stringify(params) },
    token,
  );
}

export async function revokeDeviceSessionApi(token: string, targetSessionId: string) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/device-session/revoke',
    { method: 'POST', body: JSON.stringify({ targetSessionId }) },
    token,
  );
}

export async function createBriefApi(
  token: string,
  input: {
    title: string;
    summary: string;
    domain: string;
    level: string;
    skills: string[];
    rubric: string[];
    deliverables: string[];
    deadline: string;
    pointsReward: number;
    prizePool: string;
    assignedDirectorId: string;
    isTeamAllowed: boolean;
  },
) {
  return apiJson<{ brief: ProjectBriefEntity; snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/briefs',
    { method: 'POST', body: JSON.stringify(input) },
    token,
  );
}

export async function assignDirectorApi(
  token: string,
  briefId: string,
  directorAccountId: string,
  expectedUpdatedAtEpochMs?: number,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/briefs/${encodeURIComponent(briefId)}/assign-director`,
    { method: 'POST', body: JSON.stringify({ directorAccountId, expectedUpdatedAtEpochMs }) },
    token,
  );
}

export async function reviewBriefApi(
  token: string,
  briefId: string,
  approve: boolean,
  adminNotes: string,
  expectedUpdatedAtEpochMs?: number,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/briefs/${encodeURIComponent(briefId)}/review`,
    { method: 'POST', body: JSON.stringify({ approve, adminNotes, expectedUpdatedAtEpochMs }) },
    token,
  );
}

export async function completeBriefApi(token: string, briefId: string) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/briefs/${encodeURIComponent(briefId)}/complete`,
    { method: 'POST', body: JSON.stringify({}) },
    token,
  );
}

export async function applyToBriefApi(token: string, briefId: string, coverNote: string, roleRequested: string) {
  return apiJson<{ application: ProjectApplicationEntity; snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/briefs/${encodeURIComponent(briefId)}/apply`,
    { method: 'POST', body: JSON.stringify({ coverNote, roleRequested }) },
    token,
  );
}

export async function decideApplicationApi(
  token: string,
  applicationId: string,
  newStatus: 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED',
  decisionNote: string,
  expectedUpdatedAtEpochMs?: number,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/applications/${encodeURIComponent(applicationId)}/decide`,
    { method: 'POST', body: JSON.stringify({ newStatus, decisionNote, expectedUpdatedAtEpochMs }) },
    token,
  );
}

export async function createProjectTaskApi(
  token: string,
  input: { briefId: string; studentAccountId: string; title: string; dueDateLabel: string },
) {
  return apiJson<{ task: ProjectTaskEntity; snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/tasks',
    { method: 'POST', body: JSON.stringify(input) },
    token,
  );
}

export async function updateStudentTaskStatusApi(
  token: string,
  taskId: string,
  newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE',
  expectedUpdatedAtEpochMs?: number,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/tasks/${encodeURIComponent(taskId)}/status`,
    { method: 'POST', body: JSON.stringify({ newStatus, expectedUpdatedAtEpochMs }) },
    token,
  );
}

export async function submitProjectWorkApi(
  token: string,
  input: { briefId: string; repoOrDemoUrl: string; understandingReport: string; skillsApplied: string[] },
) {
  return apiJson<{ submission: WorkSubmissionEntity; snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/submissions',
    { method: 'POST', body: JSON.stringify(input) },
    token,
  );
}

export async function evaluateSubmissionApi(
  token: string,
  submissionId: string,
  input: {
    score: number;
    feedback: string;
    decision: 'VERIFIED' | 'REVISION_REQUESTED' | 'REJECTED';
    rejectionReason: string;
    expectedEvaluatedAtEpochMs?: number;
  },
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/submissions/${encodeURIComponent(submissionId)}/evaluate`,
    { method: 'POST', body: JSON.stringify(input) },
    token,
  );
}

export async function verifyCompanyApi(
  token: string,
  companyId: string,
  approve: boolean,
  tier: CompanyEntity['verificationTier'],
  notes: string,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/companies/${encodeURIComponent(companyId)}/verify`,
    { method: 'POST', body: JSON.stringify({ approve, tier, notes }) },
    token,
  );
}

export async function setUserActiveApi(token: string, accountId: string, active: boolean) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/users/${encodeURIComponent(accountId)}/active`,
    { method: 'POST', body: JSON.stringify({ active }) },
    token,
  );
}

export async function submitModerationReportApi(token: string, targetType: string, targetId: string, reason: string) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/reports',
    { method: 'POST', body: JSON.stringify({ targetType, targetId, reason }) },
    token,
  );
}

export async function resolveReportApi(
  token: string,
  reportId: string,
  status: 'RESOLVED' | 'DISMISSED',
  resolutionNote: string,
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/reports/${encodeURIComponent(reportId)}/resolve`,
    { method: 'POST', body: JSON.stringify({ status, resolutionNote }) },
    token,
  );
}

export async function updateStudentProfileApi(
  token: string,
  input: {
    displayName: string;
    domain: string;
    country: string;
    skills: string[];
    targetSkills: string[];
    goalsSummary: string;
    weeklyHours: number;
  },
) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/profile',
    { method: 'POST', body: JSON.stringify(input) },
    token,
  );
}

export async function markAllNotificationsReadApi(token: string) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    '/api/ecosystem/notifications/read-all',
    { method: 'POST', body: JSON.stringify({}) },
    token,
  );
}

export async function markNotificationReadApi(token: string, notificationId: string) {
  return apiJson<{ snapshot: CentralEcosystemSnapshot }>(
    `/api/ecosystem/notifications/${encodeURIComponent(notificationId)}/read`,
    { method: 'POST', body: JSON.stringify({}) },
    token,
  );
}

export function connectEcosystemEventStream(
  token: string,
  handlers: {
    onConnected: () => void;
    onSync: (event: { revision: number; kind: string; summary: string }) => void;
    onError: () => void;
  },
): () => void {
  const controller = new AbortController();

  (async () => {
    try {
      const response = await fetch('/api/events', {
        method: 'GET',
        headers: { Authorization: `Bearer ${token}` },
        signal: controller.signal,
      });
      if (!response.ok || !response.body) {
        handlers.onError();
        return;
      }
      const reader = response.body.getReader();
      const decoder = new TextDecoder();
      let buffer = '';

      while (!controller.signal.aborted) {
        const { value, done } = await reader.read();
        if (done) break;
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split('\n');
        buffer = lines.pop() ?? '';
        for (const line of lines) {
          const trimmed = line.trim();
          if (!trimmed.startsWith('data:')) continue;
          const payload = trimmed.slice('data:'.length).trim();
          if (!payload) continue;
          try {
            const parsed = JSON.parse(payload) as {
              type?: string;
              revision?: number;
              kind?: string;
              summary?: string;
            };
            if (parsed.type === 'connected') {
              handlers.onConnected();
            } else if (parsed.type === 'sync') {
              handlers.onSync({
                revision: parsed.revision ?? 0,
                kind: parsed.kind ?? 'sync',
                summary: parsed.summary ?? '',
              });
            }
          } catch {
            // ignore partial heartbeat
          }
        }
      }
      if (!controller.signal.aborted) {
        handlers.onError();
      }
    } catch {
      if (!controller.signal.aborted) {
        handlers.onError();
      }
    }
  })();

  return () => {
    controller.abort();
  };
}
