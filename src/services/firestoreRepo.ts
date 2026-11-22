import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  getDocs,
  onSnapshot,
  orderBy,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where,
  writeBatch,
  type Unsubscribe,
} from 'firebase/firestore';
import { db, handleFirestoreError, OperationType } from '../lib/firebase';
import type {
  AuditLogEntry,
  CareerGoal,
  ChallengeSubmission,
  CoachMessage,
  CommunityComment,
  CommunityPost,
  PointLedgerEntry,
  RoadmapMilestone,
  SkillGap,
  SkillGapAnalysisResult,
  Team,
  UserBadge,
  UserProfile,
} from '../types/skillbridge';

function tsToMs(val: unknown): number {
  if (!val) return Date.now();
  if (typeof val === 'object' && val !== null && 'toMillis' in val && typeof (val as { toMillis: () => number }).toMillis === 'function') {
    return (val as { toMillis: () => number }).toMillis();
  }
  return Date.now();
}

export function subscribeUserProfile(uid: string, onChange: (profile: UserProfile | null) => void): Unsubscribe {
  const path = `users/${uid}`;
  return onSnapshot(
    doc(db, 'users', uid),
    (snap) => {
      if (!snap.exists()) {
        onChange(null);
        return;
      }
      const d = snap.data();
      onChange({
        userId: String(d.userId || uid),
        displayName: String(d.displayName || 'SkillBridge Learner'),
        currentRole: String(d.currentRole || 'Junior Developer'),
        experienceLevel: String(d.experienceLevel || 'Junior'),
        country: String(d.country || 'Egypt'),
        timezone: String(d.timezone || 'Africa/Cairo'),
        domain: String(d.domain || 'Backend & Cloud'),
        careerLevel: String(d.careerLevel || 'Junior'),
        planTier: String(d.planTier || 'Starter — Free'),
        roleMode: String(d.roleMode || 'LEARNER'),
        weeklyHours: Number(d.weeklyHours ?? 10),
        currentSkills: Array.isArray(d.currentSkills) ? d.currentSkills.map(String) : [],
        targetSkills: Array.isArray(d.targetSkills) ? d.targetSkills.map(String) : [],
        goalsSummary: String(d.goalsSummary || ''),
        points: Number(d.points ?? 0),
        streakDays: Number(d.streakDays ?? 1),
        passportPublic: Boolean(d.passportPublic ?? true),
        showRepoLinks: Boolean(d.showRepoLinks ?? true),
        preferredLanguage: d.preferredLanguage === 'AR' ? 'AR' : 'EN',
        darkTheme: Boolean(d.darkTheme ?? false),
        onboardingCompleted: Boolean(d.onboardingCompleted ?? false),
        savedChallengeIds: Array.isArray(d.savedChallengeIds) ? d.savedChallengeIds.map(String) : [],
        savedPostIds: Array.isArray(d.savedPostIds) ? d.savedPostIds.map(String) : [],
        followedTags: Array.isArray(d.followedTags) ? d.followedTags.map(String) : [],
        mutedAuthors: Array.isArray(d.mutedAuthors) ? d.mutedAuthors.map(String) : [],
        aiPersonalizationEnabled: Boolean(d.aiPersonalizationEnabled ?? true),
        aiContextSharingEnabled: Boolean(d.aiContextSharingEnabled ?? true),
        emailNotifications: Boolean(d.emailNotifications ?? true),
        pushNotifications: Boolean(d.pushNotifications ?? true),
        teamMentionsNotifications: Boolean(d.teamMentionsNotifications ?? true),
        lastRoute: String(d.lastRoute || 'home'),
        verifiedSkillCount: Number(d.verifiedSkillCount ?? 0),
      });
    },
    (error) => handleFirestoreError(error, OperationType.GET, path),
  );
}

export async function ensureFirestoreUserProfile(uid: string, displayName: string) {
  const path = `users/${uid}`;
  try {
    await setDoc(
      doc(db, 'users', uid),
      {
        userId: uid,
        displayName: (displayName || 'SkillBridge Learner').slice(0, 80),
        currentRole: 'Aspiring Engineer',
        experienceLevel: 'Junior',
        country: 'Egypt',
        timezone: 'Africa/Cairo',
        domain: 'Backend & Cloud',
        careerLevel: 'Junior',
        planTier: 'Starter — Free',
        roleMode: 'LEARNER',
        weeklyHours: 10,
        currentSkills: ['Kotlin', 'SQL'],
        targetSkills: ['Distributed Systems', 'React TypeScript'],
        goalsSummary: 'Build verified portfolio projects across Web, Mobile & Desktop.',
        points: 0,
        streakDays: 1,
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
        verifiedSkillCount: 0,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      },
      { merge: true },
    );
  } catch (error) {
    handleFirestoreError(error, OperationType.WRITE, path);
  }
}

export async function updateFirestoreUserPreferences(uid: string, updates: Partial<UserProfile>) {
  const path = `users/${uid}`;
  try {
    await updateDoc(doc(db, 'users', uid), {
      ...updates,
      updatedAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.UPDATE, path);
  }
}

export function subscribeUserGoals(uid: string, onChange: (goals: CareerGoal[]) => void): Unsubscribe {
  const path = `users/${uid}/goals`;
  return onSnapshot(
    query(collection(db, 'users', uid, 'goals'), orderBy('createdAt', 'desc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            targetRole: String(data.targetRole || ''),
            experienceLevel: String(data.experienceLevel || ''),
            weeklyHours: Number(data.weeklyHours ?? 10),
            readinessScore: Number(data.readinessScore ?? 0),
            executiveSummary: String(data.executiveSummary || ''),
            currentSkills: Array.isArray(data.currentSkills) ? data.currentSkills.map(String) : [],
            matchedSkills: Array.isArray(data.matchedSkills) ? data.matchedSkills.map(String) : [],
            isActive: Boolean(data.isActive ?? false),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export function subscribeUserGaps(uid: string, onChange: (gaps: SkillGap[]) => void): Unsubscribe {
  const path = `users/${uid}/gaps`;
  return onSnapshot(
    collection(db, 'users', uid, 'gaps'),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          const pr = String(data.priority || 'MEDIUM');
          return {
            id: d.id,
            userId: String(data.userId || uid),
            goalId: String(data.goalId || ''),
            skillName: String(data.skillName || ''),
            priority: pr === 'HIGH' || pr === 'LOW' ? pr : 'MEDIUM',
            currentLevel: String(data.currentLevel || ''),
            targetLevel: String(data.targetLevel || ''),
            whyItMatters: String(data.whyItMatters || ''),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export function subscribeUserMilestones(uid: string, onChange: (items: RoadmapMilestone[]) => void): Unsubscribe {
  const path = `users/${uid}/milestones`;
  return onSnapshot(
    query(collection(db, 'users', uid, 'milestones'), orderBy('phaseOrder', 'asc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            goalId: String(data.goalId || ''),
            phaseOrder: Number(data.phaseOrder ?? 1),
            title: String(data.title || ''),
            description: String(data.description || ''),
            estimatedHours: Number(data.estimatedHours ?? 4),
            practiceProject: String(data.practiceProject || ''),
            resourceTip: String(data.resourceTip || ''),
            completed: Boolean(data.completed ?? false),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export async function saveAnalysisPlanFirestore(
  uid: string,
  params: {
    targetRole: string;
    experienceLevel: string;
    weeklyHours: number;
    currentSkills: string[];
    analysis: SkillGapAnalysisResult;
  },
): Promise<string> {
  const path = `users/${uid}/goals`;
  try {
    const batch = writeBatch(db);
    const existingGoals = await getDocs(collection(db, 'users', uid, 'goals'));
    for (const gDoc of existingGoals.docs) {
      batch.update(gDoc.ref, { isActive: false, updatedAt: serverTimestamp() });
    }

    const goalRef = doc(collection(db, 'users', uid, 'goals'));
    batch.set(goalRef, {
      userId: uid,
      targetRole: params.targetRole.slice(0, 100),
      experienceLevel: params.experienceLevel.slice(0, 40),
      weeklyHours: Math.max(1, Math.min(60, params.weeklyHours)),
      readinessScore: Math.max(0, Math.min(100, params.analysis.readinessScore)),
      executiveSummary: params.analysis.executiveSummary.slice(0, 2000),
      currentSkills: params.currentSkills.slice(0, 30),
      matchedSkills: params.analysis.matchedSkills.slice(0, 30),
      isActive: true,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });

    for (const gap of params.analysis.skillGaps.slice(0, 8)) {
      const gapRef = doc(collection(db, 'users', uid, 'gaps'));
      batch.set(gapRef, {
        userId: uid,
        goalId: goalRef.id,
        skillName: gap.skillName.slice(0, 80),
        priority: ['HIGH', 'MEDIUM', 'LOW'].includes(gap.priority) ? gap.priority : 'MEDIUM',
        currentLevel: gap.currentLevel.slice(0, 60),
        targetLevel: gap.targetLevel.slice(0, 60),
        whyItMatters: gap.whyItMatters.slice(0, 500),
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      });
    }

    for (const ms of params.analysis.milestones.slice(0, 8)) {
      const msRef = doc(collection(db, 'users', uid, 'milestones'));
      batch.set(msRef, {
        userId: uid,
        goalId: goalRef.id,
        phaseOrder: Math.max(1, Math.min(50, ms.phaseOrder)),
        title: ms.title.slice(0, 120),
        description: ms.description.slice(0, 800),
        estimatedHours: Math.max(1, Math.min(200, ms.estimatedHours)),
        practiceProject: ms.practiceProject.slice(0, 500),
        resourceTip: ms.resourceTip.slice(0, 300),
        completed: false,
        createdAt: serverTimestamp(),
        updatedAt: serverTimestamp(),
      });
    }

    await batch.commit();
    return goalRef.id;
  } catch (error) {
    handleFirestoreError(error, OperationType.WRITE, path);
  }
}

export async function toggleMilestoneCompletedFirestore(uid: string, milestoneId: string, completed: boolean) {
  const path = `users/${uid}/milestones/${milestoneId}`;
  try {
    await updateDoc(doc(db, 'users', uid, 'milestones', milestoneId), {
      completed,
      updatedAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.UPDATE, path);
  }
}

export function subscribeUserSubmissions(uid: string, onChange: (subs: ChallengeSubmission[]) => void): Unsubscribe {
  const path = `users/${uid}/submissions`;
  return onSnapshot(
    query(collection(db, 'users', uid, 'submissions'), orderBy('createdAt', 'desc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            challengeId: String(data.challengeId || ''),
            challengeTitle: String(data.challengeTitle || ''),
            companyName: String(data.companyName || ''),
            category: (data.category as ChallengeSubmission['category']) || 'CHALLENGE',
            roleInTeam: String(data.roleInTeam || 'Solo Builder'),
            skillsApplied: Array.isArray(data.skillsApplied) ? data.skillsApplied.map(String) : [],
            repoOrDemoUrl: String(data.repoOrDemoUrl || ''),
            understandingReport: String(data.understandingReport || ''),
            status: (data.status as ChallengeSubmission['status']) || 'PENDING',
            rejectionReason: String(data.rejectionReason || ''),
            mentorFeedback: String(data.mentorFeedback || ''),
            score: Number(data.score ?? 0),
            pointsAwarded: Number(data.pointsAwarded ?? 0),
            rewardAmount: String(data.rewardAmount || ''),
            rewardStatus: (data.rewardStatus as ChallengeSubmission['rewardStatus']) || 'PENDING',
            isPublicOnPassport: Boolean(data.isPublicOnPassport ?? true),
            verificationCode: String(data.verificationCode || ''),
            isDemoSample: false,
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export async function submitChallengeSolutionFirestore(
  uid: string,
  sub: Omit<ChallengeSubmission, 'id' | 'userId' | 'isDemoSample' | 'createdAtMs'>,
) {
  const path = `users/${uid}/submissions`;
  try {
    const ref = await addDoc(collection(db, 'users', uid, 'submissions'), {
      userId: uid,
      ...sub,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
    return ref.id;
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }
}

export function subscribeUserBadges(uid: string, onChange: (badges: UserBadge[]) => void): Unsubscribe {
  const path = `users/${uid}/badges`;
  return onSnapshot(
    collection(db, 'users', uid, 'badges'),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            badgeId: String(data.badgeId || d.id),
            title: String(data.title || ''),
            description: String(data.description || ''),
            criteria: String(data.criteria || ''),
            pointsAwarded: Number(data.pointsAwarded ?? 0),
            isShareable: Boolean(data.isShareable ?? true),
            unlockedAtMs: tsToMs(data.unlockedAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export function subscribePointLedger(uid: string, onChange: (entries: PointLedgerEntry[]) => void): Unsubscribe {
  const path = `users/${uid}/pointLedger`;
  return onSnapshot(
    query(collection(db, 'users', uid, 'pointLedger'), orderBy('createdAt', 'desc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            txId: String(data.txId || d.id),
            sourceType: (data.sourceType as PointLedgerEntry['sourceType']) || 'CHALLENGE',
            sourceId: String(data.sourceId || ''),
            title: String(data.title || ''),
            points: Number(data.points ?? 0),
            cashAmount: String(data.cashAmount || ''),
            status: (data.status as PointLedgerEntry['status']) || 'PENDING',
            reason: String(data.reason || ''),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export function subscribeAuditLogs(uid: string, onChange: (logs: AuditLogEntry[]) => void): Unsubscribe {
  const path = `users/${uid}/auditLogs`;
  return onSnapshot(
    query(collection(db, 'users', uid, 'auditLogs'), orderBy('createdAt', 'desc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            userId: String(data.userId || uid),
            actionType: String(data.actionType || ''),
            summary: String(data.summary || ''),
            actorRole: String(data.actorRole || ''),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export function subscribeCommunityPosts(onChange: (posts: CommunityPost[]) => void): Unsubscribe {
  const path = 'communityPosts';
  return onSnapshot(
    query(collection(db, 'communityPosts'), where('isHidden', '==', false), orderBy('createdAt', 'desc')),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            authorId: String(data.authorId || ''),
            authorName: String(data.authorName || ''),
            authorLevel: String(data.authorLevel || 'Junior'),
            authorVerified: Boolean(data.authorVerified ?? false),
            postType: String(data.postType || 'Technical Question'),
            title: String(data.title || ''),
            body: String(data.body || ''),
            tags: Array.isArray(data.tags) ? data.tags.map(String) : [],
            helpfulCount: Number(data.helpfulCount ?? 0),
            commentCount: Number(data.commentCount ?? 0),
            acceptedCommentId: String(data.acceptedCommentId || ''),
            isHidden: Boolean(data.isHidden ?? false),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export async function createCommunityPostFirestore(
  uid: string,
  post: Omit<CommunityPost, 'id' | 'authorId' | 'helpfulCount' | 'commentCount' | 'acceptedCommentId' | 'isHidden' | 'createdAtMs'>,
) {
  const path = 'communityPosts';
  try {
    const ref = await addDoc(collection(db, 'communityPosts'), {
      authorId: uid,
      authorName: post.authorName.slice(0, 80),
      authorLevel: post.authorLevel.slice(0, 40),
      authorVerified: Boolean(post.authorVerified),
      postType: post.postType.slice(0, 40),
      title: post.title.slice(0, 180),
      body: post.body.slice(0, 5000),
      tags: post.tags.slice(0, 8),
      helpfulCount: 0,
      commentCount: 0,
      acceptedCommentId: '',
      isHidden: false,
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
    return ref.id;
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }
}

export function subscribePostComments(postId: string, onChange: (comments: CommunityComment[]) => void): Unsubscribe {
  const path = `communityPosts/${postId}/comments`;
  return onSnapshot(
    query(collection(db, 'communityPosts', postId, 'comments'), where('postId', '==', postId)),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            postId,
            parentCommentId: String(data.parentCommentId || ''),
            authorId: String(data.authorId || ''),
            authorName: String(data.authorName || ''),
            authorLevel: String(data.authorLevel || 'Junior'),
            authorVerified: Boolean(data.authorVerified ?? false),
            body: String(data.body || ''),
            helpfulCount: Number(data.helpfulCount ?? 0),
            isBestAnswer: Boolean(data.isBestAnswer ?? false),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export async function addCommentFirestore(
  uid: string,
  postId: string,
  comment: { authorName: string; authorLevel: string; authorVerified: boolean; body: string; parentCommentId?: string },
) {
  const path = `communityPosts/${postId}/comments`;
  try {
    await addDoc(collection(db, 'communityPosts', postId, 'comments'), {
      postId,
      parentCommentId: comment.parentCommentId || '',
      authorId: uid,
      authorName: comment.authorName.slice(0, 80),
      authorLevel: comment.authorLevel.slice(0, 40),
      authorVerified: Boolean(comment.authorVerified),
      body: comment.body.slice(0, 3000),
      helpfulCount: 0,
      isBestAnswer: false,
      createdAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }
}

export function subscribeRecruitingTeams(onChange: (teams: Team[]) => void): Unsubscribe {
  const path = 'teams';
  return onSnapshot(
    query(collection(db, 'teams'), where('lookingForTeammates', '==', true)),
    (snap) => {
      onChange(
        snap.docs.map((d) => {
          const data = d.data();
          return {
            id: d.id,
            captainId: String(data.captainId || ''),
            captainName: String(data.captainName || ''),
            name: String(data.name || ''),
            description: String(data.description || ''),
            domain: String(data.domain || ''),
            level: String(data.level || 'Mid-level'),
            maxMembers: Number(data.maxMembers ?? 4),
            memberIds: Array.isArray(data.memberIds) ? data.memberIds.map(String) : [],
            memberNames: Array.isArray(data.memberNames) ? data.memberNames.map(String) : [],
            requiredSkills: Array.isArray(data.requiredSkills) ? data.requiredSkills.map(String) : [],
            rolesNeeded: Array.isArray(data.rolesNeeded) ? data.rolesNeeded.map(String) : [],
            challengeTitle: String(data.challengeTitle || ''),
            lookingForTeammates: Boolean(data.lookingForTeammates ?? true),
            sprintProgress: Number(data.sprintProgress ?? 0),
            createdAtMs: tsToMs(data.createdAt),
          };
        }),
      );
    },
    (error) => handleFirestoreError(error, OperationType.LIST, path),
  );
}

export async function createTeamFirestore(
  uid: string,
  team: Omit<Team, 'id' | 'captainId' | 'memberIds' | 'createdAtMs'>,
) {
  const path = 'teams';
  try {
    const ref = await addDoc(collection(db, 'teams'), {
      captainId: uid,
      captainName: team.captainName.slice(0, 80),
      name: team.name.slice(0, 100),
      description: team.description.slice(0, 1000),
      domain: team.domain.slice(0, 60),
      level: team.level.slice(0, 40),
      maxMembers: Math.max(2, Math.min(12, team.maxMembers)),
      memberIds: [uid],
      memberNames: team.memberNames.slice(0, 12),
      requiredSkills: team.requiredSkills.slice(0, 15),
      rolesNeeded: team.rolesNeeded.slice(0, 10),
      challengeTitle: team.challengeTitle.slice(0, 150),
      lookingForTeammates: team.lookingForTeammates,
      sprintProgress: Math.max(0, Math.min(100, team.sprintProgress)),
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
    });
    return ref.id;
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }
}

export async function joinTeamFirestore(team: Team, uid: string, displayName: string) {
  const path = `teams/${team.id}`;
  if (team.memberIds.includes(uid)) return;
  try {
    await updateDoc(doc(db, 'teams', team.id), {
      memberIds: [...team.memberIds, uid],
      memberNames: [...team.memberNames, displayName],
      updatedAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.UPDATE, path);
  }
}

export async function deleteUserAccountDataFirestore(uid: string) {
  const subcollections = ['goals', 'gaps', 'milestones', 'coachMessages', 'submissions', 'badges', 'pointLedger', 'auditLogs'];
  try {
    for (const sub of subcollections) {
      const snap = await getDocs(collection(db, 'users', uid, sub));
      for (const item of snap.docs) {
        await deleteDoc(item.ref);
      }
    }
    await deleteDoc(doc(db, 'users', uid));
  } catch (error) {
    handleFirestoreError(error, OperationType.DELETE, `users/${uid}`);
  }
}

export async function saveCoachMessageFirestore(
  uid: string,
  msg: Omit<CoachMessage, 'id' | 'userId' | 'createdAtMs'>,
) {
  const path = `users/${uid}/coachMessages`;
  try {
    await addDoc(collection(db, 'users', uid, 'coachMessages'), {
      userId: uid,
      goalId: msg.goalId || 'default',
      mode: msg.mode,
      sender: msg.sender,
      content: msg.content.slice(0, 6000),
      createdAt: serverTimestamp(),
    });
  } catch (error) {
    handleFirestoreError(error, OperationType.CREATE, path);
  }
}
