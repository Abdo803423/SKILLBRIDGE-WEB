import { BADGE_DEFINITIONS } from '../data/ecosystemData';
import type {
  BadgeDefinition,
  BadgeProgressItem,
  ChallengeSubmission,
  CommunityComment,
  CommunityPost,
  RoadmapMilestone,
  Team,
  TeamTask,
  UserBadge,
  UserProfile,
} from '../types/skillbridge';

const SECRET_PATTERNS = ['AIza', 'sk-', 'Bearer ', '-----BEGIN', 'ghp_', 'AKIA'];

export function containsSecretLeak(text: string): boolean {
  return SECRET_PATTERNS.some((p) => text.includes(p));
}

export function deterministicLedgerTxId(sourceType: string, sourceId: string): string {
  const safeId = sourceId.replace(/[^a-zA-Z0-9_-]/g, '_');
  return `${sourceType.toLowerCase()}_${safeId}`;
}

export function computeLevelFromEvidence(totalPoints: number, verifiedSubmissionsCount: number): string {
  if (totalPoints >= 3500 && verifiedSubmissionsCount >= 10) return 'Director / Leadership';
  if (totalPoints >= 1500 && verifiedSubmissionsCount >= 5) return 'Senior';
  if (totalPoints >= 500 && verifiedSubmissionsCount >= 2) return 'Mid-level';
  return 'Junior';
}

export function computeVerifiedSkillCount(submissions: ChallengeSubmission[]): number {
  const safeSubs = Array.isArray(submissions) ? submissions : [];
  const realVerified = safeSubs.filter((s) => !s?.isDemoSample && !String(s?.id ?? '').startsWith('seed_') && s?.status === 'VERIFIED');
  const uniqueSkills = new Set<string>();
  for (const sub of realVerified) {
    const skills = Array.isArray(sub.skillsApplied) ? sub.skillsApplied : [];
    for (const skill of skills) {
      if (typeof skill === 'string' && skill.trim()) uniqueSkills.add(skill.trim().toLowerCase());
    }
  }
  return uniqueSkills.size;
}

export function evaluateBadgeProgress(params: {
  profile: UserProfile;
  submissions: ChallengeSubmission[];
  earnedBadges: UserBadge[];
  authoredPosts: CommunityPost[];
  authoredComments: CommunityComment[];
  userTeams: Team[];
  userTeamTasks: TeamTask[];
  completedMilestones: RoadmapMilestone[];
}): BadgeProgressItem[] {
  const profile = params.profile;
  const submissions = Array.isArray(params.submissions) ? params.submissions : [];
  const earnedBadges = Array.isArray(params.earnedBadges) ? params.earnedBadges : [];
  const authoredPosts = Array.isArray(params.authoredPosts) ? params.authoredPosts : [];
  const authoredComments = Array.isArray(params.authoredComments) ? params.authoredComments : [];
  const userTeams = Array.isArray(params.userTeams) ? params.userTeams : [];
  const userTeamTasks = Array.isArray(params.userTeamTasks) ? params.userTeamTasks : [];
  const completedMilestones = Array.isArray(params.completedMilestones) ? params.completedMilestones : [];

  const earnedMap = new Map(earnedBadges.map((b) => [b.badgeId, b]));
  const realVerified = submissions.filter((s) => !s?.isDemoSample && !String(s?.id ?? '').startsWith('seed_') && s?.status === 'VERIFIED');
  const verifiedSkillsCount = computeVerifiedSkillCount(submissions);
  const completedTasksCount = userTeamTasks.filter((t) => t?.status === 'DONE').length;

  return BADGE_DEFINITIONS.map((def: BadgeDefinition) => {
    const unlockedEntry = earnedMap.get(def.id);
    let current = 0;

    switch (def.id) {
      case 'first_challenge':
        current = realVerified.length;
        break;
      case 'problem_solver':
        current = realVerified.filter((s) => s.score >= 85).length;
        break;
      case 'team_player':
        current = Math.min(1, userTeams.length) + Math.min(2, completedTasksCount);
        break;
      case 'community_helper':
        current = authoredPosts.length + authoredComments.length;
        break;
      case 'reverse_eng_explorer':
        current = realVerified.filter((s) => s.category === 'REVERSE_ENG').length;
        break;
      case 'consistent_builder':
        current = completedMilestones.length + realVerified.length;
        break;
      case 'verified_skill':
        current = verifiedSkillsCount;
        break;
      case 'mentor_contributor':
        current = authoredComments.filter((c) => c.isBestAnswer).length;
        break;
      case 'top_mena_performer':
        current = profile?.points ?? 0;
        break;
      default:
        current = 0;
    }

    const capped = Math.min(current, def.targetCount);
    const pct = def.targetCount > 0 ? Math.min(100, Math.round((capped * 100) / def.targetCount)) : 0;
    const isUnlocked = Boolean(unlockedEntry) || current >= def.targetCount;
    const remaining = Math.max(0, def.targetCount - capped);

    return {
      definition: def,
      currentCount: capped,
      targetCount: def.targetCount,
      progressPercent: isUnlocked ? 100 : pct,
      isUnlocked,
      unlockedEntry,
      remainingLabel: isUnlocked
        ? { en: 'Unlocked', ar: 'مفتوحة' }
        : {
            en: `${remaining} more to unlock`,
            ar: `متبقي ${remaining} للفتح`,
          },
    };
  });
}
