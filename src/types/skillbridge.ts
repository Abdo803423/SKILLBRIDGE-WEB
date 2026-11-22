export type AppLanguage = 'EN' | 'AR';

export interface LocalizedText {
  en: string;
  ar: string;
}

export function getLocalizedText(text: LocalizedText, lang: AppLanguage): string {
  return lang === 'AR' ? text.ar : text.en;
}

export type AccountRole = 'PLATFORM_ADMIN' | 'STUDENT' | 'COMPANY' | 'COMPANY_DIRECTOR';

export type AppRoleMode = 'LEARNER' | 'MENTOR' | 'DIRECTOR' | 'COMPANY' | 'ADMIN';

export type PreviewStateMode = 'NORMAL' | 'LOADING' | 'EMPTY' | 'ERROR' | 'LOCKED';

export type VerificationTier = 'UNVERIFIED' | 'BASIC' | 'BUSINESS' | 'TRUSTED';

export interface SessionPrincipal {
  accountId: string;
  loginId: string;
  displayName: string;
  email: string;
  role: AccountRole;
  companyId: string;
  isGoogleLinked: boolean;
}

export interface AccountEntity {
  id: string;
  loginId: string;
  displayName: string;
  email: string;
  passwordSalt: string;
  passwordHash: string;
  role: AccountRole;
  companyId: string;
  isActive: boolean;
  careerLevel: string;
  domain: string;
  country: string;
  points: number;
  streakDays: number;
  verifiedCount: number;
  skillsCsv: string;
  targetSkillsCsv: string;
  goalsSummary: string;
  weeklyHours: number;
  profileCompletion: number;
  createdAtEpochMs: number;
}

export interface CompanyEntity {
  id: string;
  ownerAccountId: string;
  name: string;
  city: string;
  domain: string;
  verificationStatus: 'PENDING' | 'VERIFIED' | 'REJECTED';
  verificationTier: VerificationTier;
  verificationNotes: string;
  rewardPolicy: string;
  ipPolicy: string;
  verifiedAtEpochMs: number;
  createdAtEpochMs: number;
}

export interface ProjectBriefEntity {
  id: string;
  companyId: string;
  companyName: string;
  createdByAccountId: string;
  assignedDirectorId: string;
  assignedDirectorName: string;
  title: string;
  summary: string;
  domain: string;
  level: string;
  skillsCsv: string;
  rubricCsv: string;
  deliverablesCsv: string;
  deadline: string;
  pointsReward: number;
  prizePool: string;
  approvalStatus: 'PENDING_APPROVAL' | 'APPROVED' | 'ACTIVE' | 'COMPLETED' | 'REJECTED';
  adminNotes: string;
  isTeamAllowed: boolean;
  createdAtEpochMs: number;
  updatedAtEpochMs: number;
}

export interface ProjectApplicationEntity {
  id: string;
  briefId: string;
  studentAccountId: string;
  studentName: string;
  studentLevel: string;
  studentSkillsCsv: string;
  coverNote: string;
  roleRequested: string;
  status: 'APPLIED' | 'SHORTLISTED' | 'ACCEPTED' | 'REJECTED';
  decisionNote: string;
  appliedAtEpochMs: number;
  updatedAtEpochMs: number;
}

export interface ProjectTaskEntity {
  id: string;
  briefId: string;
  assignedStudentId: string;
  assignedStudentName: string;
  assignedDirectorId: string;
  title: string;
  milestoneOrder: number;
  dueDateLabel: string;
  isOverdue: boolean;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
  createdAtEpochMs: number;
  updatedAtEpochMs: number;
}

export interface WorkSubmissionEntity {
  id: string;
  briefId: string;
  briefTitle: string;
  companyName: string;
  studentAccountId: string;
  studentName: string;
  directorAccountId: string;
  repoOrDemoUrl: string;
  understandingReport: string;
  skillsAppliedCsv: string;
  status: 'PENDING_REVIEW' | 'VERIFIED' | 'REVISION_REQUESTED' | 'REJECTED';
  score: number;
  pointsAwarded: number;
  feedback: string;
  rejectionReason: string;
  verificationCode: string;
  isPublicOnPassport: boolean;
  submittedAtEpochMs: number;
  evaluatedAtEpochMs: number;
}

export interface PlatformNotificationEntity {
  id: string;
  recipientAccountId: string;
  category: string;
  titleEn: string;
  titleAr: string;
  bodyEn: string;
  bodyAr: string;
  isRead: boolean;
  createdAtEpochMs: number;
}

export interface PlatformActivityEntity {
  id: string;
  actorAccountId: string;
  actorRole: string;
  actionType: string;
  summary: string;
  targetId: string;
  createdAtEpochMs: number;
}

export interface ModerationReportEntity {
  id: string;
  reporterAccountId: string;
  targetType: string;
  targetId: string;
  reason: string;
  status: 'OPEN' | 'RESOLVED' | 'DISMISSED';
  resolutionNote: string;
  createdAtEpochMs: number;
}

export interface DeviceSessionEntity {
  id: string;
  accountId: string;
  deviceType: 'MOBILE' | 'DESKTOP_LAPTOP';
  deviceLabel: string;
  windowWidthDp: number;
  lastActiveEpochMs: number;
  revoked: boolean;
}

// --- Student Ecosystem Models (Firestore + Catalog) ---

export interface UserProfile {
  userId: string;
  displayName: string;
  currentRole: string;
  experienceLevel: string;
  country: string;
  timezone: string;
  domain: string;
  careerLevel: string;
  planTier: string;
  roleMode: string;
  weeklyHours: number;
  currentSkills: string[];
  targetSkills: string[];
  goalsSummary: string;
  points: number;
  streakDays: number;
  passportPublic: boolean;
  showRepoLinks: boolean;
  preferredLanguage: AppLanguage;
  darkTheme: boolean;
  onboardingCompleted: boolean;
  savedChallengeIds: string[];
  savedPostIds: string[];
  followedTags: string[];
  mutedAuthors: string[];
  aiPersonalizationEnabled: boolean;
  aiContextSharingEnabled: boolean;
  emailNotifications: boolean;
  pushNotifications: boolean;
  teamMentionsNotifications: boolean;
  lastRoute: string;
  verifiedSkillCount: number;
}

export interface CareerGoal {
  id: string;
  userId: string;
  targetRole: string;
  experienceLevel: string;
  weeklyHours: number;
  readinessScore: number;
  executiveSummary: string;
  currentSkills: string[];
  matchedSkills: string[];
  isActive: boolean;
  createdAtMs: number;
}

export interface SkillGap {
  id: string;
  userId: string;
  goalId: string;
  skillName: string;
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
  currentLevel: string;
  targetLevel: string;
  whyItMatters: string;
}

export interface RoadmapMilestone {
  id: string;
  userId: string;
  goalId: string;
  phaseOrder: number;
  title: string;
  description: string;
  estimatedHours: number;
  practiceProject: string;
  resourceTip: string;
  completed: boolean;
}

export interface SkillGapAnalysisResult {
  readinessScore: number;
  executiveSummary: string;
  matchedSkills: string[];
  skillGaps: Array<{
    skillName: string;
    priority: 'HIGH' | 'MEDIUM' | 'LOW';
    currentLevel: string;
    targetLevel: string;
    whyItMatters: string;
  }>;
  milestones: Array<{
    phaseOrder: number;
    title: string;
    description: string;
    estimatedHours: number;
    practiceProject: string;
    resourceTip: string;
  }>;
}

export interface CoachMessage {
  id: string;
  userId: string;
  goalId: string;
  mode: 'COACH' | 'INTERVIEW' | 'AGENT';
  sender: 'USER' | 'AI';
  content: string;
  createdAtMs: number;
}

export interface CompanyPartner {
  id: string;
  name: string;
  domain: string;
  tier: VerificationTier;
  verificationReason: LocalizedText;
  verifiedDate: string;
  city: string;
  publishedChallenges: number;
  completionRate: number;
  avgMentorRating: number;
  rewardPolicy: LocalizedText;
  ipPolicy: LocalizedText;
}

export interface ChallengeItem {
  id: string;
  title: LocalizedText;
  summary: LocalizedText;
  companyId: string;
  companyName: string;
  companyTier: VerificationTier;
  category: 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG';
  domain: string;
  level: string;
  isPaid: boolean;
  rewardLabel: string;
  pointsReward: number;
  deadline: string;
  durationHours: number;
  isTeamAllowed: boolean;
  skills: string[];
  rubric: LocalizedText[];
  deliverables: LocalizedText[];
  ipPolicy: LocalizedText;
  aiPolicy: LocalizedText;
  simulationPrompt: LocalizedText;
}

export interface ContestProblem {
  code: string;
  title: LocalizedText;
  difficulty: string;
  points: number;
  solvedPercent: number;
}

export interface ContestItem {
  id: string;
  title: LocalizedText;
  organizer: string;
  organizerTier: VerificationTier;
  status: 'LIVE' | 'UPCOMING' | 'ENDED';
  startsOrEndsIn: LocalizedText;
  prizePool: string;
  participantsCount: number;
  isTeamContest: boolean;
  domain: string;
  problems: ContestProblem[];
  editorialSummary: LocalizedText;
}

export interface ReverseEngineeringLab {
  id: string;
  title: LocalizedText;
  targetSystem: string;
  authorizedOrg: string;
  level: string;
  pointsReward: number;
  summary: LocalizedText;
  allowedActions: LocalizedText[];
  prohibitedActions: LocalizedText[];
  sampleArtifactTrace: string;
  understandingChecklist: LocalizedText[];
}

export interface CommunityPost {
  id: string;
  authorId: string;
  authorName: string;
  authorLevel: string;
  authorVerified: boolean;
  postType: string;
  title: string;
  body: string;
  tags: string[];
  helpfulCount: number;
  commentCount: number;
  acceptedCommentId: string;
  isHidden: boolean;
  createdAtMs: number;
}

export interface CommunityComment {
  id: string;
  postId: string;
  parentCommentId: string;
  authorId: string;
  authorName: string;
  authorLevel: string;
  authorVerified: boolean;
  body: string;
  helpfulCount: number;
  isBestAnswer: boolean;
  createdAtMs: number;
}

export interface Team {
  id: string;
  captainId: string;
  captainName: string;
  name: string;
  description: string;
  domain: string;
  level: string;
  maxMembers: number;
  memberIds: string[];
  memberNames: string[];
  requiredSkills: string[];
  rolesNeeded: string[];
  challengeTitle: string;
  lookingForTeammates: boolean;
  sprintProgress: number;
  createdAtMs: number;
}

export interface TeamTask {
  id: string;
  teamId: string;
  memberIds: string[];
  title: string;
  assigneeName: string;
  role: string;
  status: 'TODO' | 'IN_PROGRESS' | 'DONE';
}

export interface ChallengeSubmission {
  id: string;
  userId: string;
  challengeId: string;
  challengeTitle: string;
  companyName: string;
  category: 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG';
  roleInTeam: string;
  skillsApplied: string[];
  repoOrDemoUrl: string;
  understandingReport: string;
  status: 'PENDING' | 'UNDER_REVIEW' | 'VERIFIED' | 'REJECTED' | 'DISPUTED';
  rejectionReason: string;
  mentorFeedback: string;
  score: number;
  pointsAwarded: number;
  rewardAmount: string;
  rewardStatus: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'PAID' | 'REJECTED' | 'DISPUTED';
  isPublicOnPassport: boolean;
  verificationCode: string;
  isDemoSample: boolean;
  createdAtMs: number;
}

export interface LeaderboardEntry {
  rank: number;
  name: string;
  country: string;
  region: string;
  domain: string;
  level: string;
  rating: number;
  points: number;
  verifiedCount: number;
  rankDelta: number;
  isTeam: boolean;
}

export interface SkillEvidenceNode {
  skill: string;
  level: string;
  proofTitle: LocalizedText;
  evaluator: string;
  verificationId: string;
}

export interface NotificationItem {
  id: string;
  category: string;
  title: LocalizedText;
  body: LocalizedText;
  timeAgo: string;
  isUnread: boolean;
}

export interface BadgeDefinition {
  id: string;
  title: LocalizedText;
  description: LocalizedText;
  criteria: LocalizedText;
  pointsAwarded: number;
  targetCount: number;
}

export interface UserBadge {
  id: string;
  userId: string;
  badgeId: string;
  title: string;
  description: string;
  criteria: string;
  pointsAwarded: number;
  isShareable: boolean;
  unlockedAtMs: number;
}

export interface BadgeProgressItem {
  definition: BadgeDefinition;
  currentCount: number;
  targetCount: number;
  progressPercent: number;
  isUnlocked: boolean;
  unlockedEntry?: UserBadge;
  remainingLabel: LocalizedText;
}

export interface PointLedgerEntry {
  id: string;
  userId: string;
  txId: string;
  sourceType: 'CHALLENGE' | 'CONTEST' | 'REVERSE_ENG' | 'BADGE' | 'COMMUNITY' | 'TEAM' | 'MENTOR';
  sourceId: string;
  title: string;
  points: number;
  cashAmount: string;
  status: 'PENDING' | 'UNDER_REVIEW' | 'APPROVED' | 'PAID' | 'REJECTED' | 'DISPUTED';
  reason: string;
  createdAtMs: number;
}

export interface AuditLogEntry {
  id: string;
  userId: string;
  actionType: string;
  summary: string;
  actorRole: string;
  createdAtMs: number;
}

export type AiAgentContextMode =
  | 'SKILL_ADVISOR'
  | 'CHALLENGE_MENTOR'
  | 'TEAM_SPRINT_PLANNER'
  | 'COMMUNITY_COACH'
  | 'REVERSE_ENG_GUIDE'
  | 'PASSPORT_REVIEWER';

export type AiAgentPendingAction =
  | {
      type: 'AddSprintTasks';
      team: Team;
      taskTitles: string[];
      role: string;
      description: LocalizedText;
    }
  | {
      type: 'UpdateTargetSkills';
      skillsToAdd: string[];
      description: LocalizedText;
    }
  | {
      type: 'DraftCommunityQuestion';
      title: string;
      body: string;
      tags: string[];
      description: LocalizedText;
    };

export interface AiAgentMessage {
  id: string;
  sender: 'USER' | 'AI';
  content: string;
  dataUsedLabel?: string;
  boundariesLabel?: string;
  feedbackPositive?: boolean | null;
  isReported?: boolean;
  pendingAction?: AiAgentPendingAction;
}
