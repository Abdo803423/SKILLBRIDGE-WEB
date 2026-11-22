import React, { createContext, useCallback, useContext, useEffect, useMemo, useRef, useState } from 'react';
import { onAuthStateChanged, type User as FirebaseUser } from 'firebase/auth';
import {
  CHALLENGES_CATALOG,
  CONTESTS_CATALOG,
  REVERSE_ENG_LABS,
  SEED_COMMENTS,
  SEED_COMMUNITY_POSTS,
  SEED_COMPANIES,
  SEED_NOTIFICATIONS,
  SEED_SUBMISSIONS,
  SEED_TEAM_TASKS,
  SEED_TEAMS,
} from '../data/ecosystemData';
import { auth, signInWithGooglePopup, signOutFirebase, testConnection } from '../lib/firebase';
import { useOnlineStatus, detectDevicePlatform } from '../hooks/usePWAInstall';
import { getStrings, type AppStrings } from '../i18n/localization';
import {
  applyToBriefApi,
  assignDirectorApi,
  completeBriefApi,
  connectEcosystemEventStream,
  createBriefApi,
  createProjectTaskApi,
  decideApplicationApi,
  evaluateSubmissionApi,
  fetchEcosystemSnapshotApi,
  fetchPublicCatalogApi,
  loginWithCredentialsApi,
  markAllNotificationsReadApi,
  markNotificationReadApi,
  registerDeviceSessionApi,
  registerStudentApi,
  resolveGoogleSessionApi,
  resolveReportApi,
  reviewBriefApi,
  revokeDeviceSessionApi,
  setUserActiveApi,
  submitModerationReportApi,
  submitProjectWorkApi,
  updateStudentProfileApi,
  updateStudentTaskStatusApi,
  verifyCompanyApi,
  type CentralEcosystemSnapshot,
} from '../services/ecosystemApi';
import {
  addCommentFirestore,
  createCommunityPostFirestore,
  createTeamFirestore,
  deleteUserAccountDataFirestore,
  ensureFirestoreUserProfile,
  joinTeamFirestore,
  saveAnalysisPlanFirestore,
  saveCoachMessageFirestore,
  subscribeAuditLogs,
  subscribeCommunityPosts,
  subscribePointLedger,
  subscribeRecruitingTeams,
  subscribeUserBadges,
  subscribeUserGaps,
  subscribeUserGoals,
  subscribeUserMilestones,
  subscribeUserProfile,
  subscribeUserSubmissions,
  submitChallengeSolutionFirestore,
  toggleMilestoneCompletedFirestore,
  updateFirestoreUserPreferences,
} from '../services/firestoreRepo';
import { containsSecretLeak } from '../services/pointsAndBadgesEngine';
import type {
  AiAgentContextMode,
  AiAgentMessage,
  AppLanguage,
  AuditLogEntry,
  CareerGoal,
  ChallengeSubmission,
  CommunityPost,
  CompanyEntity,
  NotificationItem,
  PointLedgerEntry,
  PreviewStateMode,
  RoadmapMilestone,
  SessionPrincipal,
  SkillGap,
  SkillGapAnalysisResult,
  Team,
  TeamTask,
  UserBadge,
  UserProfile,
} from '../types/skillbridge';

export type ActiveRoute =
  | 'welcome'
  | 'downloads'
  | 'home'
  | 'discover'
  | 'challengeDetail'
  | 'contests'
  | 'reverseEng'
  | 'community'
  | 'postDetail'
  | 'teams'
  | 'teamWorkspace'
  | 'passport'
  | 'analysis'
  | 'badges'
  | 'wallet'
  | 'leaderboard'
  | 'briefs'
  | 'coach'
  | 'search'
  | 'notifications'
  | 'blueprint'
  | 'settings'
  | 'adminHome'
  | 'adminQueues'
  | 'adminUsers'
  | 'adminLogs'
  | 'companyHome'
  | 'companyBriefs'
  | 'companyApplicants'
  | 'companyTalent'
  | 'companyAnalytics'
  | 'directorHome'
  | 'directorStudents'
  | 'directorReviews'
  | 'directorHistory'
  | 'directorProjects';

interface SkillBridgeContextValue {
  language: AppLanguage;
  isArabic: boolean;
  darkTheme: boolean;
  strings: AppStrings;
  toggleLanguage: () => void;
  toggleTheme: () => void;

  isOnline: boolean;
  sseConnected: boolean;
  lastSyncedMs: number;
  syncError: string | null;

  route: ActiveRoute;
  setRoute: (route: ActiveRoute) => void;
  selectedChallengeId: string;
  setSelectedChallengeId: (id: string) => void;
  selectedPostId: string;
  setSelectedPostId: (id: string) => void;
  selectedTeamId: string;
  setSelectedTeamId: (id: string) => void;

  // Auth & Session
  firebaseUser: FirebaseUser | null;
  authReady: boolean;
  isGuestPreview: boolean;
  enterGuestPreview: () => void;
  session: SessionPrincipal | null;
  bearerToken: string | null;
  authBusy: boolean;
  authError: string | null;
  loginWithCredentials: (loginId: string, password: string) => Promise<void>;
  registerStudent: (params: { loginId: string; displayName: string; email: string; password: string }) => Promise<void>;
  signInWithGoogle: () => Promise<void>;
  signOutAll: () => Promise<void>;
  clearAuthError: () => void;

  // Central Backend Snapshot (RBAC Role Portals + Shared Live Data)
  snapshot: CentralEcosystemSnapshot | null;
  publicBriefs: CentralEcosystemSnapshot['briefs'];
  publicCompanies: CompanyEntity[];
  refreshSnapshot: () => Promise<void>;

  // Central Backend Actions
  actionBusy: boolean;
  actionToast: string | null;
  clearActionToast: () => void;
  showToast: (msg: string) => void;

  createBrief: Parameters<typeof createBriefApi>[1] extends infer T ? (input: T) => Promise<void> : never;
  assignDirector: (briefId: string, directorAccountId: string, expectedUpdatedAtEpochMs?: number) => Promise<void>;
  reviewBrief: (briefId: string, approve: boolean, adminNotes: string, expectedUpdatedAtEpochMs?: number) => Promise<void>;
  completeBrief: (briefId: string) => Promise<void>;
  applyToBrief: (briefId: string, coverNote: string, roleRequested: string) => Promise<void>;
  decideApplication: (
    applicationId: string,
    newStatus: 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED',
    decisionNote: string,
    expectedUpdatedAtEpochMs?: number,
  ) => Promise<void>;
  createProjectTask: (input: { briefId: string; studentAccountId: string; title: string; dueDateLabel: string }) => Promise<void>;
  updateStudentTaskStatus: (taskId: string, newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE', expectedUpdatedAtEpochMs?: number) => Promise<void>;
  submitProjectWork: (input: { briefId: string; repoOrDemoUrl: string; understandingReport: string; skillsApplied: string[] }) => Promise<void>;
  evaluateSubmission: (
    submissionId: string,
    input: {
      score: number;
      feedback: string;
      decision: 'VERIFIED' | 'REVISION_REQUESTED' | 'REJECTED';
      rejectionReason: string;
      expectedEvaluatedAtEpochMs?: number;
    },
  ) => Promise<void>;
  verifyCompany: (companyId: string, approve: boolean, tier: CompanyEntity['verificationTier'], notes: string) => Promise<void>;
  setUserActive: (accountId: string, active: boolean) => Promise<void>;
  submitModerationReport: (targetType: string, targetId: string, reason: string) => Promise<void>;
  resolveReport: (reportId: string, status: 'RESOLVED' | 'DISMISSED', resolutionNote: string) => Promise<void>;
  updateStudentProfile: (input: {
    displayName: string;
    domain: string;
    country: string;
    skills: string[];
    targetSkills: string[];
    goalsSummary: string;
    weeklyHours: number;
  }) => Promise<void>;
  markAllNotificationsRead: () => Promise<void>;
  markNotificationRead: (id: string) => Promise<void>;
  revokeDeviceSession: (targetSessionId: string) => Promise<void>;
  currentDeviceSessionId: string;

  // Student Ecosystem State (Firestore + Catalog)
  profile: UserProfile;
  goals: CareerGoal[];
  activeGoal: CareerGoal | null;
  gaps: SkillGap[];
  milestones: RoadmapMilestone[];
  submissions: ChallengeSubmission[];
  badges: UserBadge[];
  pointLedger: PointLedgerEntry[];
  auditLogs: AuditLogEntry[];
  communityPosts: CommunityPost[];
  teams: Team[];
  teamTasks: Record<string, TeamTask[]>;
  localNotifications: NotificationItem[];

  saveChallengeToggle: (challengeId: string) => Promise<void>;
  saveAnalysisPlan: (params: {
    targetRole: string;
    experienceLevel: string;
    weeklyHours: number;
    currentSkills: string[];
    analysis: SkillGapAnalysisResult;
  }) => Promise<void>;
  toggleMilestone: (milestoneId: string, completed: boolean) => Promise<void>;
  submitCatalogChallenge: (params: {
    challengeId: string;
    challengeTitle: string;
    companyName: string;
    category: 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG';
    roleInTeam: string;
    skillsApplied: string[];
    repoOrDemoUrl: string;
    understandingReport: string;
  }) => Promise<void>;
  createCommunityPost: (params: { postType: string; title: string; body: string; tags: string[] }) => Promise<void>;
  addCommentToPost: (postId: string, body: string, parentCommentId?: string) => Promise<void>;
  createTeam: (params: { name: string; description: string; domain: string; challengeTitle: string; rolesNeeded: string[]; requiredSkills: string[] }) => Promise<void>;
  joinTeam: (team: Team) => Promise<void>;
  saveCoachMessage: (mode: 'COACH' | 'INTERVIEW' | 'AGENT', sender: 'USER' | 'AI', content: string) => Promise<void>;
  deleteAccountAndReset: () => Promise<void>;

  // AI Agent Drawer
  aiDrawerOpen: boolean;
  setAiDrawerOpen: (open: boolean) => void;
  aiAgentMode: AiAgentContextMode;
  setAiAgentMode: (mode: AiAgentContextMode) => void;
  aiMessages: AiAgentMessage[];
  setAiMessages: React.Dispatch<React.SetStateAction<AiAgentMessage[]>>;

  // UI Preview Inspector
  previewStateMode: PreviewStateMode;
  setPreviewStateMode: (mode: PreviewStateMode) => void;
}

const DEFAULT_PROFILE: UserProfile = {
  userId: 'guest',
  displayName: 'SkillBridge Explorer',
  currentRole: 'Aspiring Software Engineer',
  experienceLevel: 'Junior',
  country: 'Egypt',
  timezone: 'Africa/Cairo',
  domain: 'Backend & Cloud',
  careerLevel: 'Mid-level',
  planTier: 'Starter — Free',
  roleMode: 'LEARNER',
  weeklyHours: 12,
  currentSkills: ['Kotlin', 'SQL', 'Python', 'TypeScript'],
  targetSkills: ['Distributed Systems', 'Time-Series Telemetry', 'JWT Security'],
  goalsSummary: 'Build mentor-verified solutions and grow verified Experience Passport credentials.',
  points: 640,
  streakDays: 6,
  passportPublic: true,
  showRepoLinks: true,
  preferredLanguage: 'EN',
  darkTheme: false,
  onboardingCompleted: true,
  savedChallengeIds: [],
  savedPostIds: [],
  followedTags: ['Backend', 'Architecture'],
  mutedAuthors: [],
  aiPersonalizationEnabled: true,
  aiContextSharingEnabled: true,
  emailNotifications: true,
  pushNotifications: true,
  teamMentionsNotifications: true,
  lastRoute: 'home',
  verifiedSkillCount: 2,
};

function routeHomeForRole(role: SessionPrincipal['role']): ActiveRoute {
  switch (role) {
    case 'PLATFORM_ADMIN':
      return 'adminHome';
    case 'COMPANY':
      return 'companyHome';
    case 'COMPANY_DIRECTOR':
      return 'directorHome';
    default:
      return 'home';
  }
}

function getOrCreateClientSessionId(): string {
  const key = 'sb_device_session_id';
  try {
    const existing = localStorage.getItem(key);
    if (existing) return existing;
    const created = `dev_${Math.random().toString(36).slice(2, 12)}`;
    localStorage.setItem(key, created);
    return created;
  } catch {
    return `dev_${Math.random().toString(36).slice(2, 12)}`;
  }
}

const SkillBridgeContext = createContext<SkillBridgeContextValue | null>(null);

export const SkillBridgeProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const isOnline = useOnlineStatus();
  const [language, setLanguage] = useState<AppLanguage>('EN');
  const [darkTheme, setDarkTheme] = useState<boolean>(false);
  const [route, setRoute] = useState<ActiveRoute>('welcome');
  const [selectedChallengeId, setSelectedChallengeId] = useState<string>(CHALLENGES_CATALOG[0].id);
  const [selectedPostId, setSelectedPostId] = useState<string>(SEED_COMMUNITY_POSTS[0].id);
  const [selectedTeamId, setSelectedTeamId] = useState<string>(SEED_TEAMS[0].id);

  const [firebaseUser, setFirebaseUser] = useState<FirebaseUser | null>(null);
  const [authReady, setAuthReady] = useState(false);
  const [isGuestPreview, setIsGuestPreview] = useState(false);
  const [session, setSession] = useState<SessionPrincipal | null>(null);
  const [bearerToken, setBearerToken] = useState<string | null>(null);
  const [authBusy, setAuthBusy] = useState(false);
  const [authError, setAuthError] = useState<string | null>(null);

  const [snapshot, setSnapshot] = useState<CentralEcosystemSnapshot | null>(null);
  const [publicBriefs, setPublicBriefs] = useState<CentralEcosystemSnapshot['briefs']>([]);
  const [publicCompanies, setPublicCompanies] = useState<CompanyEntity[]>([]);
  const [sseConnected, setSseConnected] = useState(false);
  const [lastSyncedMs, setLastSyncedMs] = useState<number>(Date.now());
  const [syncError, setSyncError] = useState<string | null>(null);

  const [actionBusy, setActionBusy] = useState(false);
  const [actionToast, setActionToast] = useState<string | null>(null);

  // Firestore Student State
  const [fsProfile, setFsProfile] = useState<UserProfile | null>(null);
  const [goals, setGoals] = useState<CareerGoal[]>([]);
  const [gaps, setGaps] = useState<SkillGap[]>([]);
  const [milestones, setMilestones] = useState<RoadmapMilestone[]>([]);
  const [fsSubmissions, setFsSubmissions] = useState<ChallengeSubmission[]>([]);
  const [badges, setBadges] = useState<UserBadge[]>([]);
  const [pointLedger, setPointLedger] = useState<PointLedgerEntry[]>([]);
  const [auditLogs, setAuditLogs] = useState<AuditLogEntry[]>([]);
  const [fsCommunityPosts, setFsCommunityPosts] = useState<CommunityPost[]>([]);
  const [fsTeams, setFsTeams] = useState<Team[]>([]);
  const [localSavedChallenges, setLocalSavedChallenges] = useState<string[]>([]);

  // AI Agent Drawer
  const [aiDrawerOpen, setAiDrawerOpen] = useState(false);
  const [aiAgentMode, setAiAgentMode] = useState<AiAgentContextMode>('SKILL_ADVISOR');
  const [aiMessages, setAiMessages] = useState<AiAgentMessage[]>([]);
  const [previewStateMode, setPreviewStateMode] = useState<PreviewStateMode>('NORMAL');

  const currentDeviceSessionId = useMemo(() => getOrCreateClientSessionId(), []);
  const toastTimerRef = useRef<number | null>(null);

  const showToast = useCallback((msg: string) => {
    setActionToast(msg);
    if (toastTimerRef.current) window.clearTimeout(toastTimerRef.current);
    toastTimerRef.current = window.setTimeout(() => setActionToast(null), 4500);
  }, []);

  const clearActionToast = useCallback(() => setActionToast(null), []);
  const clearAuthError = useCallback(() => setAuthError(null), []);

  // Sync <html> lang, dir, and dark class
  useEffect(() => {
    const html = document.documentElement;
    html.lang = language === 'AR' ? 'ar' : 'en';
    html.dir = language === 'AR' ? 'rtl' : 'ltr';
    if (darkTheme) {
      html.classList.add('dark');
    } else {
      html.classList.remove('dark');
    }
  }, [language, darkTheme]);

  // Initial Firestore connectivity verification
  useEffect(() => {
    testConnection().catch(() => {});
  }, []);

  // Fetch public catalog summary
  const refreshPublicCatalog = useCallback(async () => {
    try {
      const data = await fetchPublicCatalogApi();
      setPublicBriefs(data.publishedBriefs);
      setPublicCompanies(data.companies);
    } catch {
      // Offline cache may still serve static data
    }
  }, []);

  useEffect(() => {
    refreshPublicCatalog();
  }, [refreshPublicCatalog]);

  // Refresh Central Snapshot when authenticated
  const refreshSnapshot = useCallback(async () => {
    if (!bearerToken) return;
    try {
      const next = await fetchEcosystemSnapshotApi(bearerToken);
      setSnapshot(next);
      setLastSyncedMs(Date.now());
      setSyncError(null);
      if (next.deviceSessions.some((d) => d.id === currentDeviceSessionId && d.revoked)) {
        setBearerToken(null);
        setSession(null);
        setSnapshot(null);
        setRoute('welcome');
        showToast(language === 'AR' ? 'تم إنهاء جلسة هذا الجهاز من حسابك.' : 'This device session was revoked remotely.');
      }
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Sync failed';
      setSyncError(msg);
    }
  }, [bearerToken, currentDeviceSessionId, language, showToast]);

  // Real-time SSE Synchronization across Web, Mobile PWA & Desktop PWA
  useEffect(() => {
    if (!bearerToken || !isOnline) {
      setSseConnected(false);
      return;
    }

    let cancelled = false;
    let reconnectTimer: number | null = null;
    let stopStream: (() => void) | null = null;

    const platform = detectDevicePlatform();
    const isElectronDesktop =
      typeof navigator !== 'undefined' && navigator.userAgent.toLowerCase().includes('electron');
    const isStandalone =
      typeof window !== 'undefined' && window.matchMedia('(display-mode: standalone)').matches;
    const resolvedLabel = isElectronDesktop
      ? `${platform.osLabel} · Native Desktop App`
      : `${platform.osLabel} · ${isStandalone ? 'Installed App' : 'Web Browser'}`;
    registerDeviceSessionApi(bearerToken, {
      sessionId: currentDeviceSessionId,
      deviceType: platform.recommendedTarget === 'MOBILE' && !isElectronDesktop ? 'MOBILE' : 'DESKTOP_LAPTOP',
      deviceLabel: resolvedLabel,
      windowWidthDp: platform.windowWidth,
    })
      .then((res) => {
        if (!cancelled) {
          setSnapshot(res.snapshot);
          setLastSyncedMs(Date.now());
        }
      })
      .catch(() => {});

    const openStream = () => {
      if (cancelled) return;
      stopStream = connectEcosystemEventStream(bearerToken, {
        onConnected: () => {
          if (!cancelled) {
            setSseConnected(true);
            refreshSnapshot();
          }
        },
        onSync: () => {
          if (!cancelled) {
            refreshSnapshot();
            refreshPublicCatalog();
          }
        },
        onError: () => {
          if (!cancelled) {
            setSseConnected(false);
            reconnectTimer = window.setTimeout(openStream, 3500);
          }
        },
      });
    };

    openStream();

    return () => {
      cancelled = true;
      if (stopStream) stopStream();
      if (reconnectTimer) window.clearTimeout(reconnectTimer);
    };
  }, [bearerToken, isOnline, currentDeviceSessionId, refreshSnapshot, refreshPublicCatalog]);

  // Recover central state automatically when returning online
  useEffect(() => {
    if (isOnline) {
      refreshPublicCatalog();
      if (bearerToken) refreshSnapshot();
    }
  }, [isOnline, bearerToken, refreshPublicCatalog, refreshSnapshot]);

  // Firebase Auth listener for Google-signed-in learners
  useEffect(() => {
    const unsub = onAuthStateChanged(auth, async (user) => {
      setFirebaseUser(user);
      setAuthReady(true);
      if (user) {
        try {
          await ensureFirestoreUserProfile(user.uid, user.displayName || user.email || 'SkillBridge Learner');
          const res = await resolveGoogleSessionApi({
            firebaseUid: user.uid,
            displayName: user.displayName || 'SkillBridge Learner',
            email: user.email || '',
          });
          setBearerToken(res.token);
          setSession(res.principal);
          setIsGuestPreview(false);
          setRoute((prev) => (prev === 'welcome' || prev === 'downloads' ? routeHomeForRole(res.principal.role) : prev));
        } catch {
          // Keep Firebase state active even if central API temporarily unreachable
        }
      }
    });
    return () => unsub();
  }, []);

  // Firestore real-time subscriptions when Firebase user is authenticated
  useEffect(() => {
    if (!authReady || !firebaseUser) {
      setFsProfile(null);
      setGoals([]);
      setGaps([]);
      setMilestones([]);
      setFsSubmissions([]);
      setBadges([]);
      setPointLedger([]);
      setAuditLogs([]);
      return;
    }

    const uid = firebaseUser.uid;
    const unsubs = [
      subscribeUserProfile(uid, (p) => {
        setFsProfile(p);
        if (p) {
          setLanguage(p.preferredLanguage);
          setDarkTheme(p.darkTheme);
        }
      }),
      subscribeUserGoals(uid, setGoals),
      subscribeUserGaps(uid, setGaps),
      subscribeUserMilestones(uid, setMilestones),
      subscribeUserSubmissions(uid, setFsSubmissions),
      subscribeUserBadges(uid, setBadges),
      subscribePointLedger(uid, setPointLedger),
      subscribeAuditLogs(uid, setAuditLogs),
      subscribeCommunityPosts(setFsCommunityPosts),
      subscribeRecruitingTeams(setFsTeams),
    ];

    return () => {
      unsubs.forEach((u) => u());
    };
  }, [authReady, firebaseUser]);

  const toggleLanguage = useCallback(() => {
    setLanguage((prev) => {
      const next: AppLanguage = prev === 'EN' ? 'AR' : 'EN';
      if (firebaseUser) {
        updateFirestoreUserPreferences(firebaseUser.uid, { preferredLanguage: next }).catch(() => {});
      }
      return next;
    });
  }, [firebaseUser]);

  const toggleTheme = useCallback(() => {
    setDarkTheme((prev) => {
      const next = !prev;
      if (firebaseUser) {
        updateFirestoreUserPreferences(firebaseUser.uid, { darkTheme: next }).catch(() => {});
      }
      return next;
    });
  }, [firebaseUser]);

  const loginWithCredentials = useCallback(async (loginId: string, password: string) => {
    setAuthBusy(true);
    setAuthError(null);
    try {
      const res = await loginWithCredentialsApi(loginId, password);
      setBearerToken(res.token);
      setSession(res.principal);
      setIsGuestPreview(false);
      const snap = await fetchEcosystemSnapshotApi(res.token);
      setSnapshot(snap);
      setLastSyncedMs(Date.now());
      setRoute(routeHomeForRole(res.principal.role));
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : 'Login failed.');
    } finally {
      setAuthBusy(false);
    }
  }, []);

  const registerStudent = useCallback(
    async (params: { loginId: string; displayName: string; email: string; password: string }) => {
      setAuthBusy(true);
      setAuthError(null);
      try {
        const res = await registerStudentApi(params);
        setBearerToken(res.token);
        setSession(res.principal);
        setIsGuestPreview(false);
        const snap = await fetchEcosystemSnapshotApi(res.token);
        setSnapshot(snap);
        setLastSyncedMs(Date.now());
        setRoute('home');
      } catch (err) {
        setAuthError(err instanceof Error ? err.message : 'Registration failed.');
      } finally {
        setAuthBusy(false);
      }
    },
    [],
  );

  const signInWithGoogle = useCallback(async () => {
    setAuthBusy(true);
    setAuthError(null);
    try {
      await signInWithGooglePopup();
    } catch (err) {
      setAuthError(err instanceof Error ? err.message : 'Google Sign-In failed.');
    } finally {
      setAuthBusy(false);
    }
  }, []);

  const enterGuestPreview = useCallback(() => {
    setIsGuestPreview(true);
    setSession(null);
    setBearerToken(null);
    setRoute('home');
  }, []);

  const signOutAll = useCallback(async () => {
    setAuthBusy(true);
    try {
      if (firebaseUser) {
        await signOutFirebase();
      }
    } finally {
      setBearerToken(null);
      setSession(null);
      setSnapshot(null);
      setIsGuestPreview(false);
      setSseConnected(false);
      setRoute('welcome');
      setAuthBusy(false);
    }
  }, [firebaseUser]);

  async function runCentralAction(fn: (token: string) => Promise<{ snapshot: CentralEcosystemSnapshot }>, successMsg: string) {
    if (!bearerToken) {
      showToast(language === 'AR' ? 'يجب تسجيل الدخول لتنفيذ هذا الإجراء.' : 'Please sign in to perform this synchronized action.');
      return;
    }
    setActionBusy(true);
    try {
      const res = await fn(bearerToken);
      setSnapshot(res.snapshot);
      setLastSyncedMs(Date.now());
      refreshPublicCatalog();
      showToast(successMsg);
    } catch (err) {
      const msg = err instanceof Error ? err.message : 'Action failed';
      showToast(msg);
      if (msg.toLowerCase().includes('conflict')) {
        refreshSnapshot();
      }
    } finally {
      setActionBusy(false);
    }
  }

  const createBrief = useCallback(
    async (input: Parameters<typeof createBriefApi>[1]) =>
      runCentralAction(
        (t) => createBriefApi(t, input),
        language === 'AR' ? 'تم إرسال المشروع لاعتماد المشرف العام.' : 'Brief submitted for Platform Admin approval.',
      ),
    [bearerToken, language],
  );

  const assignDirector = useCallback(
    async (briefId: string, directorAccountId: string, expectedUpdatedAtEpochMs?: number) =>
      runCentralAction(
        (t) => assignDirectorApi(t, briefId, directorAccountId, expectedUpdatedAtEpochMs),
        language === 'AR' ? 'تم إسناد المشرف بنجاح.' : 'Director assigned and notified.',
      ),
    [bearerToken, language],
  );

  const reviewBrief = useCallback(
    async (briefId: string, approve: boolean, adminNotes: string, expectedUpdatedAtEpochMs?: number) =>
      runCentralAction(
        (t) => reviewBriefApi(t, briefId, approve, adminNotes, expectedUpdatedAtEpochMs),
        approve
          ? language === 'AR'
            ? 'تم اعتماد ونشر المشروع.'
            : 'Project brief approved and published.'
          : language === 'AR'
            ? 'تم رفض مسودة المشروع.'
            : 'Project brief rejected.',
      ),
    [bearerToken, language],
  );

  const completeBrief = useCallback(
    async (briefId: string) =>
      runCentralAction(
        (t) => completeBriefApi(t, briefId),
        language === 'AR' ? 'تم إكمال المشروع.' : 'Project marked completed.',
      ),
    [bearerToken, language],
  );

  const applyToBrief = useCallback(
    async (briefId: string, coverNote: string, roleRequested: string) =>
      runCentralAction(
        (t) => applyToBriefApi(t, briefId, coverNote, roleRequested),
        language === 'AR' ? 'تم إرسال طلب التحاقك.' : 'Application submitted and synced.',
      ),
    [bearerToken, language],
  );

  const decideApplication = useCallback(
    async (
      applicationId: string,
      newStatus: 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED',
      decisionNote: string,
      expectedUpdatedAtEpochMs?: number,
    ) =>
      runCentralAction(
        (t) => decideApplicationApi(t, applicationId, newStatus, decisionNote, expectedUpdatedAtEpochMs),
        language === 'AR' ? `تم تحديث حالة الطلب إلى ${newStatus}.` : `Application marked ${newStatus}.`,
      ),
    [bearerToken, language],
  );

  const createProjectTask = useCallback(
    async (input: { briefId: string; studentAccountId: string; title: string; dueDateLabel: string }) =>
      runCentralAction(
        (t) => createProjectTaskApi(t, input),
        language === 'AR' ? 'تمت إضافة المهمة للطالب.' : 'Milestone task created & synced.',
      ),
    [bearerToken, language],
  );

  const updateStudentTaskStatus = useCallback(
    async (taskId: string, newStatus: 'TODO' | 'IN_PROGRESS' | 'DONE', expectedUpdatedAtEpochMs?: number) =>
      runCentralAction(
        (t) => updateStudentTaskStatusApi(t, taskId, newStatus, expectedUpdatedAtEpochMs),
        language === 'AR' ? 'تم تحديث حالة المهمة.' : 'Task status synchronized.',
      ),
    [bearerToken, language],
  );

  const submitProjectWork = useCallback(
    async (input: { briefId: string; repoOrDemoUrl: string; understandingReport: string; skillsApplied: string[] }) =>
      runCentralAction(
        (t) => submitProjectWorkApi(t, input),
        language === 'AR' ? 'تم رفع التسليم للمراجعة.' : 'Deliverable submitted for Director review.',
      ),
    [bearerToken, language],
  );

  const evaluateSubmission = useCallback(
    async (
      submissionId: string,
      input: {
        score: number;
        feedback: string;
        decision: 'VERIFIED' | 'REVISION_REQUESTED' | 'REJECTED';
        rejectionReason: string;
        expectedEvaluatedAtEpochMs?: number;
      },
    ) =>
      runCentralAction(
        (t) => evaluateSubmissionApi(t, submissionId, input),
        language === 'AR' ? 'تم حفظ التقييم وتحديث سجل الخبرة.' : 'Evaluation saved and synced across platforms.',
      ),
    [bearerToken, language],
  );

  const verifyCompany = useCallback(
    async (companyId: string, approve: boolean, tier: CompanyEntity['verificationTier'], notes: string) =>
      runCentralAction(
        (t) => verifyCompanyApi(t, companyId, approve, tier, notes),
        language === 'AR' ? 'تم تحديث توثيق الشركة.' : 'Company verification status updated.',
      ),
    [bearerToken, language],
  );

  const setUserActive = useCallback(
    async (accountId: string, active: boolean) =>
      runCentralAction(
        (t) => setUserActiveApi(t, accountId, active),
        language === 'AR' ? 'تم تحديث حالة الحساب.' : 'Account status updated.',
      ),
    [bearerToken, language],
  );

  const submitModerationReport = useCallback(
    async (targetType: string, targetId: string, reason: string) =>
      runCentralAction(
        (t) => submitModerationReportApi(t, targetType, targetId, reason),
        language === 'AR' ? 'تم إرسال البلاغ للمراجعة.' : 'Report submitted to moderation queue.',
      ),
    [bearerToken, language],
  );

  const resolveReport = useCallback(
    async (reportId: string, status: 'RESOLVED' | 'DISMISSED', resolutionNote: string) =>
      runCentralAction(
        (t) => resolveReportApi(t, reportId, status, resolutionNote),
        language === 'AR' ? 'تم إغلاق البلاغ.' : 'Moderation report updated.',
      ),
    [bearerToken, language],
  );

  const updateStudentProfile = useCallback(
    async (input: {
      displayName: string;
      domain: string;
      country: string;
      skills: string[];
      targetSkills: string[];
      goalsSummary: string;
      weeklyHours: number;
    }) =>
      runCentralAction(
        (t) => updateStudentProfileApi(t, input),
        language === 'AR' ? 'تم حفظ وتحديث ملفك الشخصي.' : 'Profile updated & synced.',
      ),
    [bearerToken, language],
  );

  const markAllNotificationsRead = useCallback(async () => {
    if (!bearerToken) return;
    await runCentralAction(
      (t) => markAllNotificationsReadApi(t),
      language === 'AR' ? 'تم تحديد جميع الإشعارات كمقروءة.' : 'All notifications marked read.',
    );
  }, [bearerToken, language]);

  const markNotificationRead = useCallback(
    async (id: string) => {
      if (!bearerToken) return;
      await runCentralAction((t) => markNotificationReadApi(t, id), '');
    },
    [bearerToken],
  );

  const revokeDeviceSession = useCallback(
    async (targetSessionId: string) => {
      await runCentralAction(
        (t) => revokeDeviceSessionApi(t, targetSessionId),
        language === 'AR' ? 'تم إبطال الجلسة البعيدة.' : 'Device session revoked.',
      );
    },
    [bearerToken, language],
  );

  // Merge Central Account + Firestore Profile so Web/Mobile/Desktop show identical user metrics
  const effectiveProfile: UserProfile = useMemo(() => {
    const centralAcc = snapshot?.account;
    if (fsProfile) {
      return {
        ...fsProfile,
        points: Math.max(fsProfile.points, centralAcc?.points ?? 0),
        verifiedSkillCount: Math.max(fsProfile.verifiedSkillCount, centralAcc?.verifiedCount ?? 0),
      };
    }
    if (centralAcc) {
      return {
        ...DEFAULT_PROFILE,
        userId: centralAcc.id,
        displayName: centralAcc.displayName,
        domain: centralAcc.domain,
        careerLevel: centralAcc.careerLevel,
        country: centralAcc.country,
        weeklyHours: centralAcc.weeklyHours,
        currentSkills: centralAcc.skillsCsv.split(',').map((s) => s.trim()).filter(Boolean),
        targetSkills: centralAcc.targetSkillsCsv.split(',').map((s) => s.trim()).filter(Boolean),
        goalsSummary: centralAcc.goalsSummary,
        points: centralAcc.points,
        streakDays: centralAcc.streakDays,
        verifiedSkillCount: centralAcc.verifiedCount,
        preferredLanguage: language,
        darkTheme,
        savedChallengeIds: localSavedChallenges,
      };
    }
    return {
      ...DEFAULT_PROFILE,
      preferredLanguage: language,
      darkTheme,
      savedChallengeIds: localSavedChallenges,
    };
  }, [fsProfile, snapshot?.account, language, darkTheme, localSavedChallenges]);

  const activeGoal = useMemo(() => goals.find((g) => g.isActive) ?? goals[0] ?? null, [goals]);

  const mergedSubmissions = useMemo(() => {
    const centralSubs: ChallengeSubmission[] = (snapshot?.submissions ?? []).map((s) => ({
      id: s.id,
      userId: s.studentAccountId,
      challengeId: s.briefId,
      challengeTitle: s.briefTitle,
      companyName: s.companyName,
      category: 'CHALLENGE',
      roleInTeam: 'Project Contributor',
      skillsApplied: s.skillsAppliedCsv.split(',').map((k) => k.trim()).filter(Boolean),
      repoOrDemoUrl: s.repoOrDemoUrl,
      understandingReport: s.understandingReport,
      status:
        s.status === 'VERIFIED'
          ? 'VERIFIED'
          : s.status === 'REJECTED'
            ? 'REJECTED'
            : s.status === 'REVISION_REQUESTED'
              ? 'DISPUTED'
              : 'UNDER_REVIEW',
      rejectionReason: s.rejectionReason,
      mentorFeedback: s.feedback,
      score: s.score,
      pointsAwarded: s.pointsAwarded,
      rewardAmount: s.pointsAwarded > 0 ? `+${s.pointsAwarded} pts` : 'Pending',
      rewardStatus: s.status === 'VERIFIED' ? 'APPROVED' : 'UNDER_REVIEW',
      isPublicOnPassport: s.isPublicOnPassport,
      verificationCode: s.verificationCode,
      isDemoSample: false,
      createdAtMs: s.submittedAtEpochMs,
    }));

    const combined = [...centralSubs, ...fsSubmissions];
    return combined.length > 0 ? combined : SEED_SUBMISSIONS;
  }, [snapshot?.submissions, fsSubmissions]);

  const mergedPosts = useMemo(
    () => (fsCommunityPosts.length > 0 ? [...fsCommunityPosts, ...SEED_COMMUNITY_POSTS] : SEED_COMMUNITY_POSTS),
    [fsCommunityPosts],
  );

  const mergedTeams = useMemo(
    () => (fsTeams.length > 0 ? [...fsTeams, ...SEED_TEAMS] : SEED_TEAMS),
    [fsTeams],
  );

  const saveChallengeToggle = useCallback(
    async (challengeId: string) => {
      if (firebaseUser && fsProfile) {
        const current = fsProfile.savedChallengeIds;
        const next = current.includes(challengeId)
          ? current.filter((c) => c !== challengeId)
          : [...current, challengeId];
        await updateFirestoreUserPreferences(firebaseUser.uid, { savedChallengeIds: next });
      } else {
        setLocalSavedChallenges((prev) =>
          prev.includes(challengeId) ? prev.filter((c) => c !== challengeId) : [...prev, challengeId],
        );
      }
    },
    [firebaseUser, fsProfile],
  );

  const saveAnalysisPlan = useCallback(
    async (params: {
      targetRole: string;
      experienceLevel: string;
      weeklyHours: number;
      currentSkills: string[];
      analysis: SkillGapAnalysisResult;
    }) => {
      if (firebaseUser) {
        await saveAnalysisPlanFirestore(firebaseUser.uid, params);
        showToast(language === 'AR' ? 'تم حفظ خارطة الطريق في السحابة.' : 'AI Roadmap saved to Firestore.');
      } else {
        const goalId = `local_goal_${Date.now()}`;
        setGoals((prev) => [
          {
            id: goalId,
            userId: session?.accountId ?? 'guest',
            targetRole: params.targetRole,
            experienceLevel: params.experienceLevel,
            weeklyHours: params.weeklyHours,
            readinessScore: params.analysis.readinessScore,
            executiveSummary: params.analysis.executiveSummary,
            currentSkills: params.currentSkills,
            matchedSkills: params.analysis.matchedSkills,
            isActive: true,
            createdAtMs: Date.now(),
          },
          ...prev.map((g) => ({ ...g, isActive: false })),
        ]);
        setGaps(
          params.analysis.skillGaps.map((g, idx) => ({
            id: `local_gap_${idx}`,
            userId: session?.accountId ?? 'guest',
            goalId,
            ...g,
          })),
        );
        setMilestones(
          params.analysis.milestones.map((m, idx) => ({
            id: `local_ms_${idx}`,
            userId: session?.accountId ?? 'guest',
            goalId,
            ...m,
            completed: false,
          })),
        );
      }
    },
    [firebaseUser, session?.accountId, language, showToast],
  );

  const toggleMilestone = useCallback(
    async (milestoneId: string, completed: boolean) => {
      if (firebaseUser) {
        await toggleMilestoneCompletedFirestore(firebaseUser.uid, milestoneId, completed);
      } else {
        setMilestones((prev) => prev.map((m) => (m.id === milestoneId ? { ...m, completed } : m)));
      }
    },
    [firebaseUser],
  );

  const submitCatalogChallenge = useCallback(
    async (params: {
      challengeId: string;
      challengeTitle: string;
      companyName: string;
      category: 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG';
      roleInTeam: string;
      skillsApplied: string[];
      repoOrDemoUrl: string;
      understandingReport: string;
    }) => {
      if (containsSecretLeak(`${params.repoOrDemoUrl}\n${params.understandingReport}`)) {
        throw new Error(getStrings(language).secretLeakWarning);
      }
      if (!firebaseUser) {
        throw new Error(getStrings(language).guestLockedReason);
      }
      await submitChallengeSolutionFirestore(firebaseUser.uid, {
        challengeId: params.challengeId,
        challengeTitle: params.challengeTitle,
        companyName: params.companyName,
        category: params.category,
        roleInTeam: params.roleInTeam,
        skillsApplied: params.skillsApplied,
        repoOrDemoUrl: params.repoOrDemoUrl,
        understandingReport: params.understandingReport,
        status: 'UNDER_REVIEW',
        rejectionReason: '',
        mentorFeedback: '',
        score: 0,
        pointsAwarded: 0,
        rewardAmount: 'Pending Rubric Review',
        rewardStatus: 'UNDER_REVIEW',
        isPublicOnPassport: true,
        verificationCode: `SB-SUB-${Math.random().toString(36).slice(2, 6).toUpperCase()}`,
      });
      showToast(language === 'AR' ? 'تم رفع الحل بأمان إلى سجل التحقق.' : 'Solution submitted securely for verification.');
    },
    [firebaseUser, language, showToast],
  );

  const createCommunityPost = useCallback(
    async (params: { postType: string; title: string; body: string; tags: string[] }) => {
      if (containsSecretLeak(`${params.title}\n${params.body}`)) {
        throw new Error(getStrings(language).secretLeakWarning);
      }
      if (!firebaseUser) {
        throw new Error(getStrings(language).guestLockedReason);
      }
      await createCommunityPostFirestore(firebaseUser.uid, {
        authorName: effectiveProfile.displayName,
        authorLevel: effectiveProfile.careerLevel,
        authorVerified: effectiveProfile.verifiedSkillCount > 0,
        postType: params.postType,
        title: params.title,
        body: params.body,
        tags: params.tags,
      });
      showToast(language === 'AR' ? 'تم نشر المشاركة في المجتمع.' : 'Post published to Community.');
    },
    [firebaseUser, effectiveProfile, language, showToast],
  );

  const addCommentToPost = useCallback(
    async (postId: string, body: string, parentCommentId = '') => {
      if (!firebaseUser) {
        throw new Error(getStrings(language).guestLockedReason);
      }
      await addCommentFirestore(firebaseUser.uid, postId, {
        authorName: effectiveProfile.displayName,
        authorLevel: effectiveProfile.careerLevel,
        authorVerified: effectiveProfile.verifiedSkillCount > 0,
        body,
        parentCommentId,
      });
      showToast(language === 'AR' ? 'تمت إضافة تعليقك.' : 'Reply posted.');
    },
    [firebaseUser, effectiveProfile, language, showToast],
  );

  const createTeam = useCallback(
    async (params: {
      name: string;
      description: string;
      domain: string;
      challengeTitle: string;
      rolesNeeded: string[];
      requiredSkills: string[];
    }) => {
      if (!firebaseUser) {
        throw new Error(getStrings(language).guestLockedReason);
      }
      await createTeamFirestore(firebaseUser.uid, {
        captainName: effectiveProfile.displayName,
        name: params.name,
        description: params.description,
        domain: params.domain,
        level: effectiveProfile.careerLevel,
        maxMembers: 4,
        memberNames: [`${effectiveProfile.displayName} (Captain)`],
        requiredSkills: params.requiredSkills,
        rolesNeeded: params.rolesNeeded,
        challengeTitle: params.challengeTitle,
        lookingForTeammates: true,
        sprintProgress: 10,
      });
      showToast(language === 'AR' ? 'تم إنشاء الفريق بنجاح.' : 'Team created and listed in Recruiting.');
    },
    [firebaseUser, effectiveProfile, language, showToast],
  );

  const joinTeam = useCallback(
    async (team: Team) => {
      if (!firebaseUser) {
        showToast(getStrings(language).guestLockedReason);
        return;
      }
      await joinTeamFirestore(team, firebaseUser.uid, effectiveProfile.displayName);
      showToast(language === 'AR' ? `انضممت إلى فريق ${team.name}` : `Joined ${team.name}`);
    },
    [firebaseUser, effectiveProfile.displayName, language, showToast],
  );

  const saveCoachMessage = useCallback(
    async (mode: 'COACH' | 'INTERVIEW' | 'AGENT', sender: 'USER' | 'AI', content: string) => {
      if (!firebaseUser) return;
      await saveCoachMessageFirestore(firebaseUser.uid, {
        goalId: activeGoal?.id ?? 'default',
        mode,
        sender,
        content,
      });
    },
    [firebaseUser, activeGoal?.id],
  );

  const deleteAccountAndReset = useCallback(async () => {
    if (firebaseUser) {
      await deleteUserAccountDataFirestore(firebaseUser.uid);
    }
    await signOutAll();
    showToast(language === 'AR' ? 'تم حذف بيانات الحساب بنجاح.' : 'Account data deleted.');
  }, [firebaseUser, signOutAll, language, showToast]);

  const strings = useMemo(() => getStrings(language), [language]);

  const value: SkillBridgeContextValue = {
    language,
    isArabic: language === 'AR',
    darkTheme,
    strings,
    toggleLanguage,
    toggleTheme,

    isOnline,
    sseConnected,
    lastSyncedMs,
    syncError,

    route,
    setRoute,
    selectedChallengeId,
    setSelectedChallengeId,
    selectedPostId,
    setSelectedPostId,
    selectedTeamId,
    setSelectedTeamId,

    firebaseUser,
    authReady,
    isGuestPreview,
    enterGuestPreview,
    session,
    bearerToken,
    authBusy,
    authError,
    loginWithCredentials,
    registerStudent,
    signInWithGoogle,
    signOutAll,
    clearAuthError,

    snapshot,
    publicBriefs,
    publicCompanies: publicCompanies.length > 0 ? publicCompanies : [],
    refreshSnapshot,

    actionBusy,
    actionToast,
    clearActionToast,
    showToast,

    createBrief,
    assignDirector,
    reviewBrief,
    completeBrief,
    applyToBrief,
    decideApplication,
    createProjectTask,
    updateStudentTaskStatus,
    submitProjectWork,
    evaluateSubmission,
    verifyCompany,
    setUserActive,
    submitModerationReport,
    resolveReport,
    updateStudentProfile,
    markAllNotificationsRead,
    markNotificationRead,
    revokeDeviceSession,
    currentDeviceSessionId,

    profile: effectiveProfile,
    goals,
    activeGoal,
    gaps,
    milestones,
    submissions: mergedSubmissions,
    badges,
    pointLedger,
    auditLogs,
    communityPosts: mergedPosts,
    teams: mergedTeams,
    teamTasks: SEED_TEAM_TASKS,
    localNotifications: SEED_NOTIFICATIONS,

    saveChallengeToggle,
    saveAnalysisPlan,
    toggleMilestone,
    submitCatalogChallenge,
    createCommunityPost,
    addCommentToPost,
    createTeam,
    joinTeam,
    saveCoachMessage,
    deleteAccountAndReset,

    aiDrawerOpen,
    setAiDrawerOpen,
    aiAgentMode,
    setAiAgentMode,
    aiMessages,
    setAiMessages,

    previewStateMode,
    setPreviewStateMode,
  };

  return <SkillBridgeContext.Provider value={value}>{children}</SkillBridgeContext.Provider>;
};

export function useSkillBridge(): SkillBridgeContextValue {
  const ctx = useContext(SkillBridgeContext);
  if (!ctx) throw new Error('useSkillBridge must be used inside SkillBridgeProvider');
  return ctx;
}

export { SEED_COMPANIES, CHALLENGES_CATALOG, CONTESTS_CATALOG, REVERSE_ENG_LABS, SEED_COMMENTS };
