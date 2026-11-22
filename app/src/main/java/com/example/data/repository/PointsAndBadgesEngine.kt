package com.example.data.repository

import com.example.core.LocalizedText
import com.example.data.model.BadgeDefinition
import com.example.data.model.BadgeProgressItem
import com.example.data.model.ChallengeSubmission
import com.example.data.model.CommunityPost
import com.example.data.model.RoadmapMilestone
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.UserBadge
import com.example.data.model.UserProfile
import com.example.data.seed.SkillBridgeEcosystemData
import com.example.data.seed.SystemThresholds

/**
 * Pure, deterministic progression & badges engine.
 * Guarantees:
 * - New users start at 0 points, 0 badges, Junior baseline.
 * - Levels update strictly from verified points & verified submission counts.
 * - Badges evaluate deterministically and use deterministic IDs for idempotency.
 */
object PointsAndBadgesEngine {

    fun computeLevelFromEvidence(totalPoints: Long, verifiedSubmissionsCount: Int): String = when {
        totalPoints >= SystemThresholds.DIRECTOR_LEVEL_MIN_POINTS &&
            verifiedSubmissionsCount >= SystemThresholds.DIRECTOR_LEVEL_MIN_VERIFIED -> "Director / Leadership"
        totalPoints >= SystemThresholds.SENIOR_LEVEL_MIN_POINTS &&
            verifiedSubmissionsCount >= SystemThresholds.SENIOR_LEVEL_MIN_VERIFIED -> "Senior"
        totalPoints >= SystemThresholds.MID_LEVEL_MIN_POINTS &&
            verifiedSubmissionsCount >= SystemThresholds.MID_LEVEL_MIN_VERIFIED -> "Mid-level"
        else -> "Junior"
    }

    fun computeVerifiedSkillCount(verifiedSubmissions: List<ChallengeSubmission>): Int =
        verifiedSubmissions
            .filter { it.status == "VERIFIED" && !it.isDemoSample }
            .flatMap { it.skillsApplied }
            .map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .distinct()
            .size

    fun evaluateBadgesProgress(
        currentUserId: String,
        profile: UserProfile?,
        submissions: List<ChallengeSubmission>,
        earnedBadges: List<UserBadge>,
        teams: List<Team>,
        teamTasks: List<TeamTask>,
        milestones: List<RoadmapMilestone>,
        communityPosts: List<CommunityPost>,
        mentorReviewCount: Int = 0
    ): List<BadgeProgressItem> {
        val realSubs = submissions.filter { !it.isDemoSample }
        val verifiedSubs = realSubs.filter { it.status == "VERIFIED" }
        val highScoreSubs = verifiedSubs.filter { it.score >= SystemThresholds.HIGH_SCORE_THRESHOLD }
        val reverseEngSubs = verifiedSubs.filter { it.category == "REVERSE_ENG" }
        val completedMilestonesCount = milestones.count { it.completed }
        val userPostsCount = communityPosts.count { it.authorId == currentUserId && !it.id.startsWith("seed_") }
        val acceptedBestAnswersCount = communityPosts.count {
            it.authorId != currentUserId && it.acceptedCommentId.isNotBlank() && !it.id.startsWith("seed_")
        }
        val joinedTeamCount = teams.count { currentUserId in it.memberIds && !it.id.startsWith("seed_") }
        val completedTeamTasks = teamTasks.count { it.status == "DONE" && currentUserId in it.memberIds }
        val verifiedSkills = computeVerifiedSkillCount(verifiedSubs)
        val totalPoints = (profile?.points ?: 0L).toInt()

        val earnedMap = earnedBadges.associateBy { it.badgeId }

        return SkillBridgeEcosystemData.badgeDefinitions.map { def ->
            val currentCount = when (def.id) {
                "first_challenge" -> verifiedSubs.size
                "problem_solver" -> highScoreSubs.size
                "team_player" -> joinedTeamCount.coerceAtMost(1) + completedTeamTasks.coerceAtMost(2)
                "community_helper" -> userPostsCount
                "reverse_eng_explorer" -> reverseEngSubs.size
                "consistent_builder" -> completedMilestonesCount + verifiedSubs.size
                "verified_skill" -> verifiedSkills
                "mentor_contributor" -> mentorReviewCount + acceptedBestAnswersCount
                "top_mena_performer" -> totalPoints
                else -> 0
            }.coerceAtLeast(0)

            val earnedEntry = earnedMap[def.id]
            val unlocked = earnedEntry != null || currentCount >= def.targetCount
            val clampedCount = currentCount.coerceAtMost(def.targetCount)
            val pct = if (unlocked) 100 else ((clampedCount.toFloat() / def.targetCount.coerceAtLeast(1)) * 100f).toInt().coerceIn(0, 99)
            val remaining = (def.targetCount - clampedCount).coerceAtLeast(0)

            val remainingText = if (unlocked) {
                LocalizedText("Unlocked • +${def.pointsAwarded} pts", "مفتوحة • +${def.pointsAwarded} نقطة")
            } else {
                when (def.id) {
                    "top_mena_performer" -> LocalizedText(
                        "$remaining pts remaining ($clampedCount/${def.targetCount})",
                        "متبقي $remaining نقطة ($clampedCount/${def.targetCount})"
                    )
                    else -> LocalizedText(
                        "$remaining step(s) left ($clampedCount/${def.targetCount})",
                        "متبقي $remaining خطوة ($clampedCount/${def.targetCount})"
                    )
                }
            }

            BadgeProgressItem(
                definition = def,
                isUnlocked = unlocked,
                currentCount = clampedCount,
                targetCount = def.targetCount,
                progressPercent = pct,
                remainingLabel = remainingText,
                unlockedEntry = earnedEntry
            )
        }
    }

    fun findNewlyEligibleBadges(
        progressItems: List<BadgeProgressItem>,
        persistedBadges: List<UserBadge>
    ): List<BadgeDefinition> {
        val persistedIds = persistedBadges.map { it.badgeId }.toSet()
        return progressItems
            .filter { it.currentCount >= it.targetCount && it.definition.id !in persistedIds }
            .map { it.definition }
    }

    fun deterministicLedgerTxId(sourceType: String, sourceId: String): String {
        val sanitizedType = sourceType.lowercase().replace(Regex("[^a-z0-9_-]"), "_")
        val sanitizedId = sourceId.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(80)
        return "tx_${sanitizedType}_$sanitizedId"
    }
}
