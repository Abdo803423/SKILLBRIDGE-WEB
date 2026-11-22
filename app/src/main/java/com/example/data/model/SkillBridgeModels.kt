package com.example.data.model

import com.example.core.LocalizedText
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val currentRole: String = "",
    val experienceLevel: String = "Junior",
    val country: String = "Egypt",
    val timezone: String = "UTC+2",
    val domain: String = "Backend & Cloud",
    val careerLevel: String = "Junior",
    val planTier: String = "Starter — Free",
    val roleMode: String = "LEARNER",
    val weeklyHours: Long = 8L,
    val currentSkills: List<String> = emptyList(),
    val targetSkills: List<String> = emptyList(),
    val goalsSummary: String = "",
    val points: Long = 0L,
    val streakDays: Long = 0L,
    val verifiedSkillCount: Long = 0L,
    val passportPublic: Boolean = true,
    val showRepoLinks: Boolean = true,
    val preferredLanguage: String = "EN",
    val darkTheme: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val savedChallengeIds: List<String> = emptyList(),
    val savedPostIds: List<String> = emptyList(),
    val followedTags: List<String> = emptyList(),
    val mutedAuthors: List<String> = emptyList(),
    val aiPersonalizationEnabled: Boolean = true,
    val aiContextSharingEnabled: Boolean = true,
    val emailNotifications: Boolean = false,
    val pushNotifications: Boolean = true,
    val teamMentionsNotifications: Boolean = true,
    val lastRoute: String = "home",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CareerGoal(
    @DocumentId val id: String = "",
    val userId: String = "",
    val targetRole: String = "",
    val experienceLevel: String = "",
    val weeklyHours: Long = 8L,
    val readinessScore: Long = 0L,
    val executiveSummary: String = "",
    val currentSkills: List<String> = emptyList(),
    val matchedSkills: List<String> = emptyList(),
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SkillGap(
    @DocumentId val id: String = "",
    val userId: String = "",
    val goalId: String = "",
    val skillName: String = "",
    val priority: String = "MEDIUM",
    val currentLevel: String = "",
    val targetLevel: String = "",
    val whyItMatters: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class RoadmapMilestone(
    @DocumentId val id: String = "",
    val userId: String = "",
    val goalId: String = "",
    val phaseOrder: Long = 1L,
    val title: String = "",
    val description: String = "",
    val estimatedHours: Long = 4L,
    val practiceProject: String = "",
    val resourceTip: String = "",
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CoachMessage(
    @DocumentId val id: String = "",
    val userId: String = "",
    val goalId: String = "general",
    val mode: String = "COACH",
    val sender: String = "USER",
    val content: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChallengeSubmission(
    @DocumentId val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    val challengeTitle: String = "",
    val companyName: String = "",
    val category: String = "CHALLENGE",
    val roleInTeam: String = "Solo Contributor",
    val skillsApplied: List<String> = emptyList(),
    val repoOrDemoUrl: String = "",
    val understandingReport: String = "",
    val status: String = "PENDING",
    val rejectionReason: String = "",
    val mentorFeedback: String = "",
    val score: Long = 0L,
    val pointsAwarded: Long = 0L,
    val rewardAmount: String = "",
    val rewardStatus: String = "PENDING",
    val isPublicOnPassport: Boolean = true,
    val verificationCode: String = "",
    val isDemoSample: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserBadge(
    @DocumentId val id: String = "",
    val userId: String = "",
    val badgeId: String = "",
    val title: String = "",
    val description: String = "",
    val criteria: String = "",
    val pointsAwarded: Long = 0L,
    val isShareable: Boolean = true,
    @ServerTimestamp val unlockedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class PointLedgerEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val txId: String = "",
    val sourceType: String = "CHALLENGE",
    val sourceId: String = "",
    val title: String = "",
    val points: Long = 0L,
    val cashAmount: String = "",
    val status: String = "APPROVED",
    val reason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class AuditLogEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val actionType: String = "",
    val summary: String = "",
    val actorRole: String = "LEARNER",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorLevel: String = "Mid-level",
    val authorVerified: Boolean = false,
    val postType: String = "Technical Question",
    val title: String = "",
    val body: String = "",
    val tags: List<String> = emptyList(),
    val helpfulCount: Long = 0L,
    val commentCount: Long = 0L,
    val acceptedCommentId: String = "",
    val isHidden: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CommunityComment(
    @DocumentId val id: String = "",
    val postId: String = "",
    val parentCommentId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorLevel: String = "Mid-level",
    val authorVerified: Boolean = false,
    val body: String = "",
    val helpfulCount: Long = 0L,
    val isBestAnswer: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Team(
    @DocumentId val id: String = "",
    val captainId: String = "",
    val captainName: String = "",
    val name: String = "",
    val description: String = "",
    val domain: String = "Full-Stack",
    val level: String = "Mid-level",
    val maxMembers: Long = 4L,
    val memberIds: List<String> = emptyList(),
    val memberNames: List<String> = emptyList(),
    val requiredSkills: List<String> = emptyList(),
    val rolesNeeded: List<String> = emptyList(),
    val challengeTitle: String = "",
    val lookingForTeammates: Boolean = true,
    val sprintProgress: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class TeamTask(
    @DocumentId val id: String = "",
    val teamId: String = "",
    val memberIds: List<String> = emptyList(),
    val title: String = "",
    val assigneeName: String = "",
    val role: String = "Developer",
    val status: String = "TODO",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ContentReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetType: String = "POST",
    val targetId: String = "",
    val reason: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class GeneratedGapItem(
    val skillName: String,
    val priority: String,
    val currentLevel: String,
    val targetLevel: String,
    val whyItMatters: String
)

data class GeneratedMilestoneItem(
    val phaseOrder: Int,
    val title: String,
    val description: String,
    val estimatedHours: Int,
    val practiceProject: String,
    val resourceTip: String
)

data class SkillCategoryScore(
    val technical: Int = 0,
    val softSkills: Int = 0,
    val domainKnowledge: Int = 0,
    val problemSolving: Int = 0,
    val communication: Int = 0
)

data class SkillGapAnalysisResult(
    val readinessScore: Int,
    val executiveSummary: String,
    val matchedSkills: List<String>,
    val skillGaps: List<GeneratedGapItem>,
    val milestones: List<GeneratedMilestoneItem>,
    val categoryScores: SkillCategoryScore = SkillCategoryScore(),
    val aiExplanation: String = "Computed from your current skills, weekly time capacity, and verified challenge benchmarks.",
    val confidencePercent: Int = 86,
    val inputsUsed: List<String> = listOf("Self-declared skills", "Target role matrix", "Weekly hours")
)

// Badges & Progression definitions
data class BadgeDefinition(
    val id: String,
    val title: LocalizedText,
    val description: LocalizedText,
    val criteria: LocalizedText,
    val pointsAwarded: Int,
    val targetCount: Int,
    val category: String
)

data class BadgeProgressItem(
    val definition: BadgeDefinition,
    val isUnlocked: Boolean,
    val currentCount: Int,
    val targetCount: Int,
    val progressPercent: Int,
    val remainingLabel: LocalizedText,
    val unlockedEntry: UserBadge? = null
)

// Contextual AI Agent models
enum class AiAgentContextMode(
    val title: LocalizedText,
    val purpose: LocalizedText,
    val boundaries: LocalizedText
) {
    SKILL_ADVISOR(
        title = LocalizedText("Skill Gap Advisor", "مستشار فجوات المهارات"),
        purpose = LocalizedText("Explains skill gaps and prioritizes next practice challenges.", "يشرح فجوات المهارات ويرتّب التحديات العملية الأنسب."),
        boundaries = LocalizedText("Does not fabricate verified credentials or guarantee hiring.", "لا يصدر شهادات وهمية ولا يضمن توظيفًا.")
    ),
    CHALLENGE_MENTOR(
        title = LocalizedText("Challenge Rubric Mentor", "مرشد معايير التحدي"),
        purpose = LocalizedText("Guides architecture choices and edge-case testing for the active challenge.", "يرشدك في قرارات التصميم واختبار الحالات الحدية للتحدي الحالي."),
        boundaries = LocalizedText("Provides hints & rubric checks only — never copy-paste final answers.", "يقدّم تلميحات ومراجعة معايير فقط دون حلول جاهزة للنسخ.")
    ),
    TEAM_SPRINT_PLANNER(
        title = LocalizedText("Team Sprint Planner", "مخطط سباقات الفريق"),
        purpose = LocalizedText("Breaks team deliverables into clear TODO sprint tasks per role.", "يقسّم تسليمات الفريق إلى مهام واضحة حسب الدور."),
        boundaries = LocalizedText("Requires your explicit confirmation before adding tasks to Firestore.", "يتطلب تأكيدك الصريح قبل إضافة أي مهام إلى لوحة الفريق.")
    ),
    COMMUNITY_COACH(
        title = LocalizedText("Community Question Coach", "مساعد صياغة أسئلة المجتمع"),
        purpose = LocalizedText("Helps turn vague errors into clear, secret-free technical questions.", "يساعدك على صياغة أسئلة تقنية واضحة وخالية من الأسرار."),
        boundaries = LocalizedText("Never auto-posts without your review and blocks leaked API keys.", "لا ينشر تلقائيًا دون مراجعتك ويمنع تسريب المفاتيح السرية.")
    ),
    REVERSE_ENG_GUIDE(
        title = LocalizedText("Ethical Sandbox Guide", "دليل المختبر الأخلاقي"),
        purpose = LocalizedText("Explains authorized sandbox traces & Understanding Report structure.", "يشرح آثار المختبر المصرّح به وبنية تقرير الفهم الإلزامي."),
        boundaries = LocalizedText("Strictly rejects exploit payloads or targeting external live hosts.", "يرفض تمامًا أي استغلال أو فحص لنطاقات خارجية حية.")
    ),
    PASSPORT_REVIEWER(
        title = LocalizedText("Passport Readiness Reviewer", "مراجع جاهزية السجل المهني"),
        purpose = LocalizedText("Summarizes your real verified submissions, badges, and next badge step.", "يلخّص تسليماتك الموثّقة الحقيقية وشاراتك والخطوة التالية."),
        boundaries = LocalizedText("Only references real verified Firestore records in your account.", "يعتمد فقط على السجلات الموثّقة الفعلية في حسابك.")
    )
}

sealed interface AiAgentPendingAction {
    val description: LocalizedText

    data class AddSprintTasks(
        val team: Team,
        val taskTitles: List<String>,
        val role: String,
        override val description: LocalizedText
    ) : AiAgentPendingAction

    data class UpdateTargetSkills(
        val skillsToAdd: List<String>,
        override val description: LocalizedText
    ) : AiAgentPendingAction

    data class DraftCommunityQuestion(
        val title: String,
        val body: String,
        val tags: List<String>,
        override val description: LocalizedText
    ) : AiAgentPendingAction
}

data class AiAgentMessage(
    val id: String,
    val sender: String,
    val content: String,
    val dataUsedLabel: String = "",
    val boundariesLabel: String = "",
    val pendingAction: AiAgentPendingAction? = null,
    val feedbackPositive: Boolean? = null,
    val isReported: Boolean = false
)

// Ecosystem domain models (Discover, Contests, Reverse Eng, Leaderboards, Companies, Notifications, Wallet)
enum class DiscoverCategory {
    CHALLENGE, PROJECT, CONTEST, REVERSE_ENG, MENTOR, TEAM, OPPORTUNITY
}

enum class VerificationTier(val badgeEn: String, val badgeAr: String) {
    UNVERIFIED("Unverified", "غير موثّقة"),
    BASIC("Basic Verified", "توثيق أساسي"),
    BUSINESS("Business Verified", "شركة موثّقة"),
    TRUSTED("Trusted Partner", "شريك موثوق")
}

data class ChallengeItem(
    val id: String,
    val category: DiscoverCategory,
    val title: LocalizedText,
    val subtitle: LocalizedText,
    val companyName: String,
    val companyTier: VerificationTier,
    val domain: String,
    val level: String,
    val isTeamAllowed: Boolean,
    val soloOrTeamLabel: LocalizedText,
    val deadline: String,
    val pointsReward: Int,
    val prizePool: String,
    val mentorName: String,
    val mentorRole: String,
    val skills: List<String>,
    val rubric: List<LocalizedText>,
    val deliverables: List<LocalizedText>,
    val aiPolicy: LocalizedText,
    val ipPolicy: LocalizedText,
    val faq: List<Pair<LocalizedText, LocalizedText>>,
    val scopeNote: LocalizedText? = null,
    val allowedRules: List<LocalizedText> = emptyList(),
    val prohibitedRules: List<LocalizedText> = emptyList(),
    val sandboxSnippet: String = "",
    val testCases: List<String> = emptyList()
)

enum class ContestStatus { LIVE, UPCOMING, ENDED }

data class ContestProblem(
    val code: String,
    val title: LocalizedText,
    val difficulty: String,
    val points: Int,
    val solvedByCount: Int,
    val status: String
)

data class ContestItem(
    val id: String,
    val title: LocalizedText,
    val organizer: String,
    val status: ContestStatus,
    val countdownLabel: LocalizedText,
    val isTeamContest: Boolean,
    val prizePool: String,
    val rulesSummary: LocalizedText,
    val problems: List<ContestProblem>,
    val editorialSummary: LocalizedText
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val country: String,
    val region: String,
    val domain: String,
    val level: String,
    val rating: Int,
    val points: Int,
    val rankDelta: Int,
    val verifiedCount: Int,
    val isTeam: Boolean = false
)

data class CompanyProfile(
    val id: String,
    val name: String,
    val city: String,
    val domain: String,
    val tier: VerificationTier,
    val verificationReason: LocalizedText,
    val verifiedDate: String,
    val publishedChallenges: Int,
    val completionRate: Int,
    val avgMentorRating: Double,
    val rewardPolicy: LocalizedText,
    val ipPolicy: LocalizedText
)

data class EcosystemNotification(
    val id: String,
    val category: String,
    val title: LocalizedText,
    val body: LocalizedText,
    val timeAgo: String,
    val unread: Boolean
)

data class RewardTransaction(
    val id: String,
    val title: LocalizedText,
    val points: Int,
    val cashAmount: String,
    val status: String,
    val dateLabel: String
)

data class EvidenceGraphNode(
    val skill: String,
    val level: String,
    val proofTitle: LocalizedText,
    val evaluator: String,
    val verificationId: String,
    val isVerified: Boolean
)
