package com.example.data.repository

import com.example.data.model.ChallengeSubmission
import com.example.data.model.Team
import com.example.data.model.TeamTask
import com.example.data.model.UserBadge
import com.example.data.model.UserProfile
import com.example.data.seed.SystemThresholds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PointsAndBadgesEngineTest {

    @Test
    fun computeLevelFromEvidence_respectsVerifiedThresholds() {
        assertEquals("Junior", PointsAndBadgesEngine.computeLevelFromEvidence(0L, 0))
        assertEquals("Junior", PointsAndBadgesEngine.computeLevelFromEvidence(600L, 1))
        assertEquals(
            "Mid-level",
            PointsAndBadgesEngine.computeLevelFromEvidence(
                SystemThresholds.MID_LEVEL_MIN_POINTS,
                SystemThresholds.MID_LEVEL_MIN_VERIFIED
            )
        )
        assertEquals(
            "Senior",
            PointsAndBadgesEngine.computeLevelFromEvidence(
                SystemThresholds.SENIOR_LEVEL_MIN_POINTS,
                SystemThresholds.SENIOR_LEVEL_MIN_VERIFIED
            )
        )
        assertEquals(
            "Director / Leadership",
            PointsAndBadgesEngine.computeLevelFromEvidence(
                SystemThresholds.DIRECTOR_LEVEL_MIN_POINTS,
                SystemThresholds.DIRECTOR_LEVEL_MIN_VERIFIED
            )
        )
    }

    @Test
    fun evaluateBadgesProgress_brandNewUserStartsAtZeroUnlocked() {
        val progress = PointsAndBadgesEngine.evaluateBadgesProgress(
            currentUserId = "uid_fresh",
            profile = UserProfile(userId = "uid_fresh", points = 0L),
            submissions = emptyList(),
            earnedBadges = emptyList(),
            teams = emptyList(),
            teamTasks = emptyList(),
            milestones = emptyList(),
            communityPosts = emptyList()
        )
        assertEquals(9, progress.size)
        assertTrue(progress.none { it.isUnlocked })
        assertTrue(progress.all { it.progressPercent == 0 })
    }

    @Test
    fun evaluateBadgesProgress_unlocksEligibleBadgesFromRealEvidenceOnly() {
        val subs = listOf(
            ChallengeSubmission(
                id = "sub1",
                userId = "uid1",
                category = "REVERSE_ENG",
                status = "VERIFIED",
                score = 92L,
                skillsApplied = listOf("JWT Security", "Threat Modeling"),
                isDemoSample = false
            ),
            ChallengeSubmission(
                id = "sub2",
                userId = "uid1",
                category = "CHALLENGE",
                status = "VERIFIED",
                score = 88L,
                skillsApplied = listOf("Kotlin", "JWT Security"),
                isDemoSample = false
            ),
            // Demo samples must NEVER count toward real badges
            ChallengeSubmission(
                id = "demo1",
                userId = "uid1",
                category = "CHALLENGE",
                status = "VERIFIED",
                score = 99L,
                skillsApplied = listOf("FakeSkill1", "FakeSkill2"),
                isDemoSample = true
            )
        )
        val team = Team(id = "team_real", memberIds = listOf("uid1"))
        val tasks = listOf(
            TeamTask(id = "t1", teamId = "team_real", memberIds = listOf("uid1"), status = "DONE"),
            TeamTask(id = "t2", teamId = "team_real", memberIds = listOf("uid1"), status = "DONE")
        )

        val progress = PointsAndBadgesEngine.evaluateBadgesProgress(
            currentUserId = "uid1",
            profile = UserProfile(userId = "uid1", points = 930L),
            submissions = subs,
            earnedBadges = emptyList(),
            teams = listOf(team),
            teamTasks = tasks,
            milestones = emptyList(),
            communityPosts = emptyList()
        )

        val byId = progress.associateBy { it.definition.id }
        assertTrue(byId.getValue("first_challenge").isUnlocked)
        assertTrue(byId.getValue("problem_solver").isUnlocked)
        assertTrue(byId.getValue("reverse_eng_explorer").isUnlocked)
        assertTrue(byId.getValue("team_player").isUnlocked)
        assertTrue(byId.getValue("verified_skill").isUnlocked)
        assertFalse(byId.getValue("top_mena_performer").isUnlocked)

        // Idempotency: already-persisted badge is excluded from newlyEligible
        val newlyWithoutPersisted = PointsAndBadgesEngine.findNewlyEligibleBadges(
            progressItems = progress,
            persistedBadges = listOf(UserBadge(id = "first_challenge", badgeId = "first_challenge"))
        )
        assertTrue(newlyWithoutPersisted.none { it.id == "first_challenge" })
        assertTrue(newlyWithoutPersisted.any { it.id == "reverse_eng_explorer" })
    }

    @Test
    fun deterministicLedgerTxId_producesStableSafeIds() {
        val id1 = PointsAndBadgesEngine.deterministicLedgerTxId("REVERSE_ENG", "sub/123")
        val id2 = PointsAndBadgesEngine.deterministicLedgerTxId("REVERSE_ENG", "sub/123")
        assertEquals("tx_reverse_eng_sub_123", id1)
        assertEquals(id1, id2)
    }
}
