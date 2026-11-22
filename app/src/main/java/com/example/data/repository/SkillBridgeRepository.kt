package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.model.AuditLogEntry
import com.example.data.model.BadgeDefinition
import com.example.data.model.CareerGoal
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CoachMessage
import com.example.data.model.CommunityComment
import com.example.data.model.CommunityPost
import com.example.data.model.PointLedgerEntry
import com.example.data.model.RoadmapMilestone
import com.example.data.model.SkillGap
import com.example.data.model.SkillGapAnalysisResult
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.UserBadge
import com.example.data.model.UserProfile
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SkillBridgeRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun userDoc(userId: String) = db.collection("users").document(userId)

    fun observeProfile(userId: String): Flow<UserProfile?> =
        userDoc(userId)
            .snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(
                        UserProfile::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                } else {
                    null
                }
            }

    fun observeGoals(userId: String): Flow<List<CareerGoal>> =
        userDoc(userId).collection("goals")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CareerGoal::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeGaps(userId: String): Flow<List<SkillGap>> =
        userDoc(userId).collection("gaps")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    SkillGap::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                )
            }

    fun observeMilestones(userId: String): Flow<List<RoadmapMilestone>> =
        userDoc(userId).collection("milestones")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    RoadmapMilestone::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.phaseOrder }
            }

    fun observeCoachMessages(userId: String): Flow<List<CoachMessage>> =
        userDoc(userId).collection("coachMessages")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CoachMessage::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }

    fun observeSubmissions(userId: String): Flow<List<ChallengeSubmission>> =
        userDoc(userId).collection("submissions")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    ChallengeSubmission::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeBadges(userId: String): Flow<List<UserBadge>> =
        userDoc(userId).collection("badges")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    UserBadge::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.unlockedAt?.seconds ?: 0L }
            }

    fun observePointLedger(userId: String): Flow<List<PointLedgerEntry>> =
        userDoc(userId).collection("pointLedger")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    PointLedgerEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeAuditLogs(userId: String): Flow<List<AuditLogEntry>> =
        userDoc(userId).collection("auditLogs")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    AuditLogEntry::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeCommunityPosts(): Flow<List<CommunityPost>> =
        db.collection("communityPosts")
            .whereEqualTo("isHidden", false)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CommunityPost::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observePostComments(postId: String): Flow<List<CommunityComment>> =
        db.collection("communityPosts").document(postId).collection("comments")
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    CommunityComment::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedWith(
                    compareByDescending<CommunityComment> { it.isBestAnswer }
                        .thenByDescending { it.helpfulCount }
                        .thenBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
                )
            }

    fun observeRecruitingTeams(): Flow<List<Team>> =
        db.collection("teams")
            .whereEqualTo("lookingForTeammates", true)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Team::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.createdAt?.seconds ?: 0L }
            }

    fun observeMyTeams(userId: String): Flow<List<Team>> =
        db.collection("teams")
            .whereArrayContains("memberIds", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    Team::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedByDescending { it.updatedAt?.seconds ?: 0L }
            }

    fun observeTeamTasks(teamId: String, userId: String): Flow<List<TeamTask>> =
        db.collection("teams").document(teamId).collection("tasks")
            .whereArrayContains("memberIds", userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    TeamTask::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ).sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }

    suspend fun upsertProfile(
        userId: String,
        displayName: String,
        currentRole: String,
        experienceLevel: String,
        country: String = "Egypt",
        timezone: String = "UTC+2",
        domain: String = "Backend & Cloud",
        careerLevel: String = "Junior",
        planTier: String = "Starter — Free",
        roleMode: String = "LEARNER",
        weeklyHours: Int = 8,
        currentSkills: List<String> = emptyList(),
        targetSkills: List<String> = emptyList(),
        goalsSummary: String = "",
        passportPublic: Boolean = true,
        showRepoLinks: Boolean = true,
        preferredLanguage: String = "EN",
        darkTheme: Boolean = false,
        onboardingCompleted: Boolean = false
    ): Result<Unit> {
        val docRef = userDoc(userId)
        return try {
            val existing = docRef.get().await()
            val safeName = displayName.trim().ifEmpty { "SkillBridge Learner" }.take(80)
            val safeRole = currentRole.trim().take(80)
            val safeExp = experienceLevel.trim().take(40)
            val safeLevel = if (careerLevel in setOf("Junior", "Mid-level", "Senior", "Director / Leadership")) careerLevel else "Junior"
            val safePlan = if (planTier in setOf("Starter — Free", "Growth — Paid", "Professional+ — Paid", "Company / Enterprise")) planTier else "Starter — Free"
            val safeRoleMode = if (roleMode in setOf("LEARNER", "MENTOR", "DIRECTOR", "COMPANY", "ADMIN")) roleMode else "LEARNER"
            val safeCurrent = currentSkills.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(30)
            val safeTarget = targetSkills.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(30)
            val safeLang = if (preferredLanguage == "AR") "AR" else "EN"

            if (existing.exists()) {
                docRef.update(
                    mapOf(
                        "displayName" to safeName,
                        "currentRole" to safeRole,
                        "experienceLevel" to safeExp,
                        "country" to country.trim().take(60),
                        "timezone" to timezone.trim().take(60),
                        "domain" to domain.trim().take(60),
                        "careerLevel" to safeLevel,
                        "planTier" to safePlan,
                        "roleMode" to safeRoleMode,
                        "weeklyHours" to weeklyHours.coerceIn(1, 80),
                        "currentSkills" to safeCurrent,
                        "targetSkills" to safeTarget,
                        "goalsSummary" to goalsSummary.trim().take(400),
                        "passportPublic" to passportPublic,
                        "showRepoLinks" to showRepoLinks,
                        "preferredLanguage" to safeLang,
                        "darkTheme" to darkTheme,
                        "onboardingCompleted" to onboardingCompleted,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                // Zero fake achievements: brand-new account starts at 0 points, 0 streak, Junior baseline.
                docRef.set(
                    mapOf(
                        "userId" to userId,
                        "displayName" to safeName,
                        "currentRole" to safeRole,
                        "experienceLevel" to safeExp,
                        "country" to country.trim().take(60),
                        "timezone" to timezone.trim().take(60),
                        "domain" to domain.trim().take(60),
                        "careerLevel" to safeLevel,
                        "planTier" to safePlan,
                        "roleMode" to safeRoleMode,
                        "weeklyHours" to weeklyHours.coerceIn(1, 80),
                        "currentSkills" to safeCurrent,
                        "targetSkills" to safeTarget,
                        "goalsSummary" to goalsSummary.trim().take(400),
                        "points" to 0,
                        "streakDays" to 0,
                        "verifiedSkillCount" to 0,
                        "passportPublic" to passportPublic,
                        "showRepoLinks" to showRepoLinks,
                        "preferredLanguage" to safeLang,
                        "darkTheme" to darkTheme,
                        "onboardingCompleted" to onboardingCompleted,
                        "savedChallengeIds" to emptyList<String>(),
                        "savedPostIds" to emptyList<String>(),
                        "followedTags" to emptyList<String>(),
                        "mutedAuthors" to emptyList<String>(),
                        "aiPersonalizationEnabled" to true,
                        "aiContextSharingEnabled" to true,
                        "emailNotifications" to false,
                        "pushNotifications" to true,
                        "teamMentionsNotifications" to true,
                        "lastRoute" to "home",
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateUserPreferences(
        userId: String,
        updates: Map<String, Any>
    ): Result<Unit> {
        val allowedKeys = setOf(
            "preferredLanguage", "darkTheme", "onboardingCompleted", "roleMode",
            "passportPublic", "showRepoLinks", "savedChallengeIds", "savedPostIds",
            "followedTags", "mutedAuthors", "aiPersonalizationEnabled", "aiContextSharingEnabled",
            "emailNotifications", "pushNotifications", "teamMentionsNotifications",
            "lastRoute", "currentSkills", "targetSkills", "domain", "country", "timezone"
        )
        val filtered = updates.filterKeys { it in allowedKeys }.toMutableMap()
        if (filtered.isEmpty()) return Result.success(Unit)
        filtered["updatedAt"] = FieldValue.serverTimestamp()
        val docRef = userDoc(userId)
        return try {
            docRef.update(filtered).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveAnalysisPlan(
        userId: String,
        targetRole: String,
        experienceLevel: String,
        weeklyHours: Int,
        currentSkills: List<String>,
        analysis: SkillGapAnalysisResult,
        existingGoalIds: List<String> = emptyList()
    ): Result<String> {
        val goalsCol = userDoc(userId).collection("goals")
        val gapsCol = userDoc(userId).collection("gaps")
        val milestonesCol = userDoc(userId).collection("milestones")
        val goalRef = goalsCol.document()
        val goalId = goalRef.id

        return try {
            val batch = db.batch()

            existingGoalIds.forEach { oldId ->
                if (oldId.isNotBlank() && oldId != goalId) {
                    batch.update(
                        goalsCol.document(oldId),
                        mapOf(
                            "isActive" to false,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }

            val sanitizedCurrent = currentSkills.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(30)
            val sanitizedMatched = analysis.matchedSkills.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(30)

            batch.set(
                goalRef,
                mapOf(
                    "userId" to userId,
                    "targetRole" to targetRole.trim().ifEmpty { "Target Role" }.take(100),
                    "experienceLevel" to experienceLevel.trim().ifEmpty { "Mid -> Senior" }.take(40),
                    "weeklyHours" to weeklyHours.coerceIn(1, 60),
                    "readinessScore" to analysis.readinessScore.coerceIn(0, 100),
                    "executiveSummary" to analysis.executiveSummary.trim().ifEmpty { "Personalized skill bridge roadmap." }.take(2000),
                    "currentSkills" to sanitizedCurrent,
                    "matchedSkills" to sanitizedMatched,
                    "isActive" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            analysis.skillGaps.take(12).forEach { gap ->
                val priority = gap.priority.uppercase().let {
                    if (it in setOf("HIGH", "MEDIUM", "LOW")) it else "MEDIUM"
                }
                batch.set(
                    gapsCol.document(),
                    mapOf(
                        "userId" to userId,
                        "goalId" to goalId,
                        "skillName" to gap.skillName.trim().ifEmpty { "Core Skill" }.take(80),
                        "priority" to priority,
                        "currentLevel" to gap.currentLevel.trim().ifEmpty { "Developing" }.take(60),
                        "targetLevel" to gap.targetLevel.trim().ifEmpty { "Proficient" }.take(60),
                        "whyItMatters" to gap.whyItMatters.trim().ifEmpty { "Essential capability for this target role." }.take(500),
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            analysis.milestones.take(12).forEachIndexed { index, milestone ->
                batch.set(
                    milestonesCol.document(),
                    mapOf(
                        "userId" to userId,
                        "goalId" to goalId,
                        "phaseOrder" to (milestone.phaseOrder.coerceIn(1, 50).takeIf { it > 0 } ?: (index + 1)),
                        "title" to milestone.title.trim().ifEmpty { "Phase ${index + 1}" }.take(120),
                        "description" to milestone.description.trim().ifEmpty { "Build practical mastery in this topic." }.take(800),
                        "estimatedHours" to milestone.estimatedHours.coerceIn(1, 200),
                        "practiceProject" to milestone.practiceProject.trim().ifEmpty { "Build a small portfolio deliverable." }.take(500),
                        "resourceTip" to milestone.resourceTip.trim().ifEmpty { "Focus on official docs and hands-on drills." }.take(300),
                        "completed" to false,
                        "createdAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }

            batch.commit().await()
            Result.success(goalId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, goalRef.path)
            Result.failure(e)
        }
    }

    suspend fun setActiveGoal(
        userId: String,
        targetGoalId: String,
        allGoalIds: List<String>
    ): Result<Unit> {
        val goalsCol = userDoc(userId).collection("goals")
        return try {
            val batch = db.batch()
            allGoalIds.forEach { id ->
                if (id.isNotBlank()) {
                    batch.update(
                        goalsCol.document(id),
                        mapOf(
                            "isActive" to (id == targetGoalId),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )
                }
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "${goalsCol.path}/$targetGoalId")
            Result.failure(e)
        }
    }

    suspend fun toggleMilestoneCompleted(
        userId: String,
        milestone: RoadmapMilestone,
        newCompleted: Boolean,
        newReadinessScore: Int? = null
    ): Result<Unit> {
        val msRef = userDoc(userId).collection("milestones").document(milestone.id)
        return try {
            val batch = db.batch()
            batch.update(
                msRef,
                mapOf(
                    "completed" to newCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            if (newReadinessScore != null && milestone.goalId.isNotBlank()) {
                val goalRef = userDoc(userId).collection("goals").document(milestone.goalId)
                batch.update(
                    goalRef,
                    mapOf(
                        "readinessScore" to newReadinessScore.coerceIn(0, 100),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, msRef.path)
            Result.failure(e)
        }
    }

    suspend fun addCustomMilestone(
        userId: String,
        goalId: String,
        phaseOrder: Int,
        title: String,
        description: String,
        estimatedHours: Int,
        practiceProject: String
    ): Result<String> {
        val msRef = userDoc(userId).collection("milestones").document()
        return try {
            msRef.set(
                mapOf(
                    "userId" to userId,
                    "goalId" to goalId,
                    "phaseOrder" to phaseOrder.coerceIn(1, 50),
                    "title" to title.trim().ifEmpty { "Custom Milestone" }.take(120),
                    "description" to description.trim().ifEmpty { "Custom study target" }.take(800),
                    "estimatedHours" to estimatedHours.coerceIn(1, 200),
                    "practiceProject" to practiceProject.trim().ifEmpty { "Complete hands-on exercise" }.take(500),
                    "resourceTip" to "Self-paced practice & review",
                    "completed" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(msRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteGoalWithChildren(
        userId: String,
        goalId: String,
        gapIds: List<String>,
        milestoneIds: List<String>
    ): Result<Unit> {
        val goalRef = userDoc(userId).collection("goals").document(goalId)
        return try {
            val batch = db.batch()
            gapIds.forEach { gid ->
                if (gid.isNotBlank()) batch.delete(userDoc(userId).collection("gaps").document(gid))
            }
            milestoneIds.forEach { mid ->
                if (mid.isNotBlank()) batch.delete(userDoc(userId).collection("milestones").document(mid))
            }
            batch.delete(goalRef)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, goalRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveCoachMessage(
        userId: String,
        goalId: String,
        mode: String,
        sender: String,
        content: String
    ): Result<String> {
        val msgRef = userDoc(userId).collection("coachMessages").document()
        val safeMode = if (mode in setOf("INTERVIEW", "COACH", "AGENT")) mode else "COACH"
        val safeSender = if (sender == "AI") "AI" else "USER"
        val safeGoalId = goalId.trim().ifEmpty { "general" }.take(128)
        return try {
            msgRef.set(
                mapOf(
                    "userId" to userId,
                    "goalId" to safeGoalId,
                    "mode" to safeMode,
                    "sender" to safeSender,
                    "content" to content.trim().ifEmpty { "..." }.take(6000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(msgRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            Result.failure(e)
        }
    }

    suspend fun clearCoachMessages(
        userId: String,
        messageIds: List<String>
    ): Result<Unit> {
        if (messageIds.isEmpty()) return Result.success(Unit)
        val col = userDoc(userId).collection("coachMessages")
        return try {
            val batch = db.batch()
            messageIds.take(400).forEach { id ->
                if (id.isNotBlank()) batch.delete(col.document(id))
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, col.path)
            Result.failure(e)
        }
    }

    suspend fun submitChallengeSolution(
        userId: String,
        challengeId: String,
        challengeTitle: String,
        companyName: String,
        category: String,
        roleInTeam: String,
        skillsApplied: List<String>,
        repoOrDemoUrl: String,
        understandingReport: String,
        pointsReward: Int,
        rewardAmount: String
    ): Result<String> {
        val subRef = userDoc(userId).collection("submissions").document()
        val safeCategory = if (category in setOf("CHALLENGE", "CONTEST", "REVERSE_ENG")) category else "CHALLENGE"
        val safeSkills = skillsApplied.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(20)
            .ifEmpty { listOf("Practical Problem Solving") }
        val verificationCode = "SB-${UUID.randomUUID().toString().take(8).uppercase()}"
        return try {
            val batch = db.batch()
            batch.set(
                subRef,
                mapOf(
                    "userId" to userId,
                    "challengeId" to challengeId.trim().ifEmpty { "ch_custom" }.take(128),
                    "challengeTitle" to challengeTitle.trim().ifEmpty { "Practical Challenge" }.take(150),
                    "companyName" to companyName.trim().ifEmpty { "SkillBridge Partner" }.take(100),
                    "category" to safeCategory,
                    "roleInTeam" to roleInTeam.trim().ifEmpty { "Solo Contributor" }.take(80),
                    "skillsApplied" to safeSkills,
                    "repoOrDemoUrl" to repoOrDemoUrl.trim().take(300),
                    "understandingReport" to understandingReport.trim().ifEmpty { "Verified understanding report." }.take(3000),
                    "status" to "UNDER_REVIEW",
                    "rejectionReason" to "",
                    "mentorFeedback" to "Submitted to rubric queue for mentor verification.",
                    "score" to 0,
                    "pointsAwarded" to pointsReward.coerceIn(0, 10000),
                    "rewardAmount" to rewardAmount.trim().take(40),
                    "rewardStatus" to "UNDER_REVIEW",
                    "isPublicOnPassport" to true,
                    "verificationCode" to verificationCode,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            val logRef = userDoc(userId).collection("auditLogs").document()
            batch.set(
                logRef,
                mapOf(
                    "userId" to userId,
                    "actionType" to "SUBMISSION_CREATED",
                    "summary" to "Submitted ${challengeTitle.take(80)} for rubric review ($verificationCode)",
                    "actorRole" to "LEARNER",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(subRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, subRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateSubmissionPassportVisibility(
        userId: String,
        submissionId: String,
        isPublic: Boolean
    ): Result<Unit> {
        val subRef = userDoc(userId).collection("submissions").document(submissionId)
        return try {
            subRef.update(
                mapOf(
                    "isPublicOnPassport" to isPublic,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, subRef.path)
            Result.failure(e)
        }
    }

    suspend fun evaluateSubmissionAsMentor(
        userId: String,
        submissionId: String,
        status: String,
        feedback: String,
        score: Int,
        pointsAwarded: Int,
        rewardStatus: String,
        rejectionReason: String = ""
    ): Result<Unit> {
        val subRef = userDoc(userId).collection("submissions").document(submissionId)
        val safeStatus = if (status in setOf("PENDING", "UNDER_REVIEW", "VERIFIED", "REJECTED", "DISPUTED")) status else "VERIFIED"
        val safeReward = if (rewardStatus in setOf("PENDING", "UNDER_REVIEW", "APPROVED", "PAID", "REJECTED", "DISPUTED")) rewardStatus else "APPROVED"
        return try {
            subRef.update(
                mapOf(
                    "status" to safeStatus,
                    "rejectionReason" to rejectionReason.trim().take(500),
                    "mentorFeedback" to feedback.trim().take(1000),
                    "score" to score.coerceIn(0, 100),
                    "pointsAwarded" to pointsAwarded.coerceIn(0, 10000),
                    "rewardStatus" to safeReward,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, subRef.path)
            Result.failure(e)
        }
    }

    /**
     * Strictly idempotent evaluation + points ledger + career level recomputation.
     * Repeated evaluation of the same submission will never duplicate points in `pointLedger` or `UserProfile.points`.
     */
    suspend fun awardSubmissionEvaluationAndSync(
        userId: String,
        submission: ChallengeSubmission,
        approve: Boolean,
        score: Int,
        feedback: String,
        rejectionReason: String = "",
        actorRole: String = "MENTOR"
    ): Result<Unit> {
        val targetStatus = if (approve) "VERIFIED" else if (rejectionReason.isNotBlank()) "REJECTED" else "DISPUTED"
        val awardedPts = if (approve) submission.pointsAwarded.toInt().coerceIn(100, 10000) else 0
        val rewardStatus = if (approve) "APPROVED" else targetStatus
        val subRef = userDoc(userId).collection("submissions").document(submission.id)
        val txId = PointsAndBadgesEngine.deterministicLedgerTxId(submission.category, submission.id)
        val txRef = userDoc(userId).collection("pointLedger").document(txId)
        val userRef = userDoc(userId)

        return try {
            subRef.update(
                mapOf(
                    "status" to targetStatus,
                    "rejectionReason" to rejectionReason.trim().take(500),
                    "mentorFeedback" to feedback.trim().take(1000),
                    "score" to score.coerceIn(0, 100),
                    "pointsAwarded" to awardedPts,
                    "rewardStatus" to rewardStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            if (approve && awardedPts > 0) {
                val existingTx = txRef.get().await()
                if (!existingTx.exists()) {
                    txRef.set(
                        mapOf(
                            "userId" to userId,
                            "txId" to txId,
                            "sourceType" to (if (submission.category in setOf("CHALLENGE", "CONTEST", "REVERSE_ENG")) submission.category else "CHALLENGE"),
                            "sourceId" to submission.id,
                            "title" to submission.challengeTitle.take(150),
                            "points" to awardedPts,
                            "cashAmount" to submission.rewardAmount.take(40),
                            "status" to "APPROVED",
                            "reason" to "Verified rubric score $score%",
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            // Recompute total points & verified count strictly from live Firestore records
            val allLedgerSnap = userDoc(userId).collection("pointLedger").get().await()
            val totalPoints = allLedgerSnap.documents.sumOf { doc ->
                val st = doc.getString("status") ?: "APPROVED"
                if (st in setOf("APPROVED", "PAID")) doc.getLong("points") ?: 0L else 0L
            }
            val allSubsSnap = userDoc(userId).collection("submissions").get().await()
            val verifiedSubs = allSubsSnap.toObjects(ChallengeSubmission::class.java).filter { it.status == "VERIFIED" }
            val verifiedSkillCount = PointsAndBadgesEngine.computeVerifiedSkillCount(verifiedSubs)
            val updatedLevel = PointsAndBadgesEngine.computeLevelFromEvidence(totalPoints, verifiedSubs.size)

            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "points" to totalPoints.coerceIn(0L, 1000000L),
                        "verifiedSkillCount" to verifiedSkillCount.coerceIn(0, 500),
                        "careerLevel" to updatedLevel,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            appendAuditLog(
                userId = userId,
                actionType = if (approve) "SUBMISSION_VERIFIED" else "SUBMISSION_$targetStatus",
                summary = if (approve) {
                    "Verified ${submission.challengeTitle.take(60)} ($score%, +$awardedPts pts)"
                } else {
                    "$targetStatus ${submission.challengeTitle.take(60)}: ${rejectionReason.ifBlank { feedback }.take(120)}"
                },
                actorRole = actorRole
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, subRef.path)
            Result.failure(e)
        }
    }

    /**
     * Strictly idempotent badge unlock using deterministic document ID `badges/{badge.id}`
     * and deterministic ledger ID `pointLedger/tx_badge_{badge.id}`.
     */
    suspend fun awardBadgeIfAbsent(
        userId: String,
        badge: BadgeDefinition,
        actorRole: String = "SYSTEM"
    ): Result<Boolean> {
        val badgeRef = userDoc(userId).collection("badges").document(badge.id)
        val txId = PointsAndBadgesEngine.deterministicLedgerTxId("BADGE", badge.id)
        val txRef = userDoc(userId).collection("pointLedger").document(txId)
        val userRef = userDoc(userId)

        return try {
            val existingBadge = badgeRef.get().await()
            if (existingBadge.exists()) {
                return Result.success(false)
            }
            badgeRef.set(
                mapOf(
                    "userId" to userId,
                    "badgeId" to badge.id,
                    "title" to badge.title.en.take(100),
                    "description" to badge.description.en.take(300),
                    "criteria" to badge.criteria.en.take(300),
                    "pointsAwarded" to badge.pointsAwarded,
                    "isShareable" to true,
                    "unlockedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val existingTx = txRef.get().await()
            if (!existingTx.exists() && badge.pointsAwarded > 0) {
                txRef.set(
                    mapOf(
                        "userId" to userId,
                        "txId" to txId,
                        "sourceType" to "BADGE",
                        "sourceId" to badge.id,
                        "title" to "Badge: ${badge.title.en}".take(150),
                        "points" to badge.pointsAwarded,
                        "cashAmount" to "",
                        "status" to "APPROVED",
                        "reason" to badge.criteria.en.take(300),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            // Recompute total points from ledger so points always match ledger sum
            val allLedgerSnap = userDoc(userId).collection("pointLedger").get().await()
            val totalPoints = allLedgerSnap.documents.sumOf { doc ->
                val st = doc.getString("status") ?: "APPROVED"
                if (st in setOf("APPROVED", "PAID")) doc.getLong("points") ?: 0L else 0L
            }
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                userRef.update(
                    mapOf(
                        "points" to totalPoints.coerceIn(0L, 1000000L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            appendAuditLog(
                userId = userId,
                actionType = "BADGE_UNLOCKED",
                summary = "Unlocked badge ${badge.title.en} (+${badge.pointsAwarded} pts)",
                actorRole = actorRole
            )
            Result.success(true)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, badgeRef.path)
            Result.failure(e)
        }
    }

    suspend fun toggleBadgeShareable(
        userId: String,
        badgeId: String,
        isShareable: Boolean
    ): Result<Unit> {
        val badgeRef = userDoc(userId).collection("badges").document(badgeId)
        return try {
            badgeRef.update(mapOf("isShareable" to isShareable)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, badgeRef.path)
            Result.failure(e)
        }
    }

    suspend fun appendAuditLog(
        userId: String,
        actionType: String,
        summary: String,
        actorRole: String = "LEARNER"
    ): Result<Unit> {
        val logRef = userDoc(userId).collection("auditLogs").document()
        return try {
            logRef.set(
                mapOf(
                    "userId" to userId,
                    "actionType" to actionType.trim().ifEmpty { "EVENT" }.take(60),
                    "summary" to summary.trim().ifEmpty { "Activity recorded" }.take(300),
                    "actorRole" to actorRole.trim().ifEmpty { "LEARNER" }.take(40),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, logRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteUserAccountData(userId: String): Result<Unit> {
        val subcollections = listOf(
            "goals", "gaps", "milestones", "coachMessages",
            "submissions", "badges", "pointLedger", "auditLogs"
        )
        return try {
            subcollections.forEach { colName ->
                val docs = userDoc(userId).collection(colName).get().await()
                if (!docs.isEmpty) {
                    docs.documents.chunked(400).forEach { chunk ->
                        val batch = db.batch()
                        chunk.forEach { doc -> batch.delete(doc.reference) }
                        batch.commit().await()
                    }
                }
            }
            userDoc(userId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userDoc(userId).path)
            Result.failure(e)
        }
    }

    suspend fun createCommunityPost(
        authorId: String,
        authorName: String,
        authorLevel: String,
        authorVerified: Boolean,
        postType: String,
        title: String,
        body: String,
        tags: List<String>
    ): Result<String> {
        val postRef = db.collection("communityPosts").document()
        val safeTags = tags.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(8)
        return try {
            postRef.set(
                mapOf(
                    "authorId" to authorId,
                    "authorName" to authorName.trim().ifEmpty { "Learner" }.take(80),
                    "authorLevel" to authorLevel.trim().ifEmpty { "Junior" }.take(40),
                    "authorVerified" to authorVerified,
                    "postType" to postType.trim().ifEmpty { "Technical Question" }.take(40),
                    "title" to title.trim().take(180),
                    "body" to body.trim().take(5000),
                    "tags" to safeTags,
                    "helpfulCount" to 1,
                    "commentCount" to 0,
                    "acceptedCommentId" to "",
                    "isHidden" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(postRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, postRef.path)
            Result.failure(e)
        }
    }

    suspend fun updatePostHelpfulCount(postId: String, newCount: Long): Result<Unit> {
        val postRef = db.collection("communityPosts").document(postId)
        return try {
            postRef.update(
                mapOf(
                    "helpfulCount" to newCount.coerceIn(-1000L, 1000000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, postRef.path)
            Result.failure(e)
        }
    }

    suspend fun addComment(
        postId: String,
        parentCommentId: String,
        authorId: String,
        authorName: String,
        authorLevel: String,
        authorVerified: Boolean,
        body: String,
        newCommentCount: Long
    ): Result<String> {
        val postRef = db.collection("communityPosts").document(postId)
        val commentRef = postRef.collection("comments").document()
        return try {
            val batch = db.batch()
            batch.set(
                commentRef,
                mapOf(
                    "postId" to postId,
                    "parentCommentId" to parentCommentId.trim().take(128),
                    "authorId" to authorId,
                    "authorName" to authorName.trim().ifEmpty { "Learner" }.take(80),
                    "authorLevel" to authorLevel.trim().ifEmpty { "Junior" }.take(40),
                    "authorVerified" to authorVerified,
                    "body" to body.trim().take(3000),
                    "helpfulCount" to 0,
                    "isBestAnswer" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.update(
                postRef,
                mapOf(
                    "commentCount" to newCommentCount.coerceIn(0L, 100000L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(commentRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun acceptBestAnswer(postId: String, commentId: String): Result<Unit> {
        val postRef = db.collection("communityPosts").document(postId)
        val commentRef = postRef.collection("comments").document(commentId)
        return try {
            val batch = db.batch()
            batch.update(commentRef, mapOf("isBestAnswer" to true))
            batch.update(
                postRef,
                mapOf(
                    "acceptedCommentId" to commentId,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, commentRef.path)
            Result.failure(e)
        }
    }

    suspend fun createTeam(
        captainId: String,
        captainName: String,
        name: String,
        description: String,
        domain: String,
        level: String,
        maxMembers: Int,
        requiredSkills: List<String>,
        rolesNeeded: List<String>,
        challengeTitle: String
    ): Result<String> {
        val teamRef = db.collection("teams").document()
        val safeCaptain = captainName.trim().ifEmpty { "Captain" }.take(80)
        return try {
            teamRef.set(
                mapOf(
                    "captainId" to captainId,
                    "captainName" to safeCaptain,
                    "name" to name.trim().take(100),
                    "description" to description.trim().take(1000),
                    "domain" to domain.trim().ifEmpty { "Backend & Cloud" }.take(60),
                    "level" to level.trim().ifEmpty { "Mid-level" }.take(40),
                    "maxMembers" to maxMembers.coerceIn(2, 12),
                    "memberIds" to listOf(captainId),
                    "memberNames" to listOf("$safeCaptain (Captain)"),
                    "requiredSkills" to requiredSkills.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(15),
                    "rolesNeeded" to rolesNeeded.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(10),
                    "challengeTitle" to challengeTitle.trim().take(150),
                    "lookingForTeammates" to true,
                    "sprintProgress" to 10,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(teamRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, teamRef.path)
            Result.failure(e)
        }
    }

    suspend fun joinTeam(
        team: Team,
        userId: String,
        userName: String
    ): Result<Unit> {
        if (userId in team.memberIds) return Result.success(Unit)
        if (team.memberIds.size >= team.maxMembers.toInt()) {
            return Result.failure(IllegalStateException("Team is already at capacity."))
        }
        val teamRef = db.collection("teams").document(team.id)
        val newIds = (team.memberIds + userId).distinct().take(12)
        val newNames = (team.memberNames + userName.trim().ifEmpty { "Member" }.take(80)).take(12)
        return try {
            teamRef.update(
                mapOf(
                    "memberIds" to newIds,
                    "memberNames" to newNames,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, teamRef.path)
            Result.failure(e)
        }
    }

    suspend fun createTeamTask(
        team: Team,
        title: String,
        assigneeName: String,
        role: String
    ): Result<String> {
        val taskRef = db.collection("teams").document(team.id).collection("tasks").document()
        return try {
            taskRef.set(
                mapOf(
                    "teamId" to team.id,
                    "memberIds" to team.memberIds.take(12),
                    "title" to title.trim().take(140),
                    "assigneeName" to assigneeName.trim().ifEmpty { "Team Member" }.take(80),
                    "role" to role.trim().ifEmpty { "Developer" }.take(60),
                    "status" to "TODO",
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(taskRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, taskRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateTeamTaskStatus(
        teamId: String,
        taskId: String,
        newStatus: String
    ): Result<Unit> {
        val safeStatus = if (newStatus in setOf("TODO", "IN_PROGRESS", "DONE")) newStatus else "TODO"
        val taskRef = db.collection("teams").document(teamId).collection("tasks").document(taskId)
        return try {
            taskRef.update(
                mapOf(
                    "status" to safeStatus,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, taskRef.path)
            Result.failure(e)
        }
    }

    suspend fun submitReport(
        reporterId: String,
        targetType: String,
        targetId: String,
        reason: String
    ): Result<String> {
        val reportRef = db.collection("reports").document()
        return try {
            reportRef.set(
                mapOf(
                    "reporterId" to reporterId,
                    "targetType" to targetType.trim().ifEmpty { "POST" }.take(40),
                    "targetId" to targetId.trim().ifEmpty { "unknown" }.take(128),
                    "reason" to reason.trim().take(1000),
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(reportRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reportRef.path)
            Result.failure(e)
        }
    }
}
