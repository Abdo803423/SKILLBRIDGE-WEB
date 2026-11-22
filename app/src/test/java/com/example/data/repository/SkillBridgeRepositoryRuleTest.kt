package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.GeneratedGapItem
import com.example.data.model.GeneratedMilestoneItem
import com.example.data.model.SkillGapAnalysisResult
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class SkillBridgeRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertProfile_authenticatedOwner_persistsAndObservesProfile() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = SkillBridgeRepository(firestore)

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(
                userId = uid,
                displayName = "Alice Dev",
                currentRole = "Mobile Engineer",
                experienceLevel = "Mid-level",
                country = "Egypt",
                domain = "Mobile & Android",
                careerLevel = "Mid-level",
                currentSkills = listOf("Kotlin", "Jetpack Compose")
            )
        }
        assertTrue(result.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals("Alice Dev", observed?.displayName)
        assertEquals("Egypt", observed?.country)
    }

    @Test
    fun saveAnalysisPlan_andToggleMilestone_succeedsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = SkillBridgeRepository(firestore)

        val analysis = SkillGapAnalysisResult(
            readinessScore = 65,
            executiveSummary = "Solid Kotlin foundation; strengthen Compose architecture.",
            matchedSkills = listOf("Kotlin", "Coroutines"),
            skillGaps = listOf(
                GeneratedGapItem(
                    skillName = "Compose Performance",
                    priority = "HIGH",
                    currentLevel = "Intermediate",
                    targetLevel = "Expert",
                    whyItMatters = "Essential for smooth 120Hz mobile UIs."
                )
            ),
            milestones = listOf(
                GeneratedMilestoneItem(
                    phaseOrder = 1,
                    title = "Audit Recompositions",
                    description = "Use Layout Inspector to eliminate unstable lambdas.",
                    estimatedHours = 4,
                    practiceProject = "Optimize a multi-tab feed screen.",
                    resourceTip = "Focus on immutable state models."
                )
            )
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.saveAnalysisPlan(
                userId = uid,
                targetRole = "Staff Android Engineer",
                experienceLevel = "Mid -> Senior",
                weeklyHours = 8,
                currentSkills = listOf("Kotlin", "Coroutines"),
                analysis = analysis
            )
        }
        assertTrue(saveResult.isSuccess)
        val goalId = saveResult.getOrThrow()

        val milestones = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeMilestones(uid).first { list -> list.any { it.goalId == goalId } }
        }
        val targetMs = milestones.first { it.goalId == goalId }

        val toggleResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.toggleMilestoneCompleted(
                userId = uid,
                milestone = targetMs,
                newCompleted = true,
                newReadinessScore = 80
            )
        }
        assertTrue(toggleResult.isSuccess)
    }

    @Test
    fun submitChallengeSolution_andEvaluate_persistsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = SkillBridgeRepository(firestore)

        val subId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.submitChallengeSolution(
                userId = uid,
                challengeId = "ch_re_jwt_lab",
                challengeTitle = "Auth Token Rotation Protocol Audit",
                companyName = "Cedars Cloud Labs",
                category = "REVERSE_ENG",
                roleInTeam = "Security Analyst",
                skillsApplied = listOf("Reverse Engineering", "JWT Security"),
                repoOrDemoUrl = "https://skillbridge.dev/sandbox/123",
                understandingReport = "Verified token family invalidation prevents replay after generation rotation.",
                pointsReward = 380,
                rewardAmount = "$300"
            ).getOrThrow()
        }

        val subs = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSubmissions(uid).first { list -> list.any { it.id == subId } }
        }
        val created = subs.first { it.id == subId }
        assertEquals("UNDER_REVIEW", created.status)

        val evalRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.evaluateSubmissionAsMentor(
                userId = uid,
                submissionId = subId,
                status = "VERIFIED",
                feedback = "Clear threat model and passing assertions.",
                score = 93,
                pointsAwarded = 380,
                rewardStatus = "APPROVED"
            )
        }
        assertTrue(evalRes.isSuccess)
    }

    @Test
    fun communityPosts_andBestAnswer_enforcesAuthorPrivilege() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SkillBridgeRepository(firestore)

        val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createCommunityPost(
                authorId = aliceUid,
                authorName = "Alice Dev",
                authorLevel = "Mid-level",
                authorVerified = true,
                postType = "Technical Question",
                title = "How to guard Firestore listeners?",
                body = "Need clean AuthStateListener flow pattern.",
                tags = listOf("Firebase", "Kotlin")
            ).getOrThrow()
        }

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobRepo = SkillBridgeRepository(firestore)
        val commentId = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.addComment(
                postId = postId,
                parentCommentId = "",
                authorId = bobUid,
                authorName = "Bob Builder",
                authorLevel = "Senior",
                authorVerified = true,
                body = "Use callbackFlow + key(user.uid) in root composable.",
                newCommentCount = 1L
            ).getOrThrow()
        }

        // Bob cannot mark his own answer as best answer on Alice's post
        val bobAccept = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.acceptBestAnswer(postId, commentId)
        }
        assertTrue(bobAccept.isFailure)

        // Alice (post author) can mark Bob's comment as best answer
        signInTestUser(ALICE_EMAIL)
        val aliceAccept = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.acceptBestAnswer(postId, commentId)
        }
        assertTrue(aliceAccept.isSuccess)
    }

    @Test
    fun teams_andSprintTasks_enforceMemberIsolation() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SkillBridgeRepository(firestore)

        val teamId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createTeam(
                captainId = aliceUid,
                captainName = "Alice Dev",
                name = "Nile Builders",
                description = "Smart City telemetry squad",
                domain = "Data Science & AI",
                level = "Mid-level",
                maxMembers = 4,
                requiredSkills = listOf("SQL", "Kotlin"),
                rolesNeeded = listOf("Backend"),
                challengeTitle = "Smart City Analytics Dashboard"
            ).getOrThrow()
        }

        val team = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeMyTeams(aliceUid).first { it.any { t -> t.id == teamId } }.first { it.id == teamId }
        }
        val taskRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createTeamTask(team, "Design schema", "Alice Dev", "Captain")
        }
        assertTrue(taskRes.isSuccess)
        val taskId = taskRes.getOrThrow()

        // Bob cannot read Alice's team task doc before joining
        signInTestUser(BOB_EMAIL)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("teams").document(teamId).collection("tasks").document(taskId).get().await()
            }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    @Test
    fun crossUserAccess_bobCannotReadAliceGoal() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = SkillBridgeRepository(firestore)

        val analysis = SkillGapAnalysisResult(
            readinessScore = 70,
            executiveSummary = "Focus on distributed systems.",
            matchedSkills = listOf("Java"),
            skillGaps = emptyList(),
            milestones = emptyList()
        )
        val goalId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveAnalysisPlan(
                userId = aliceUid,
                targetRole = "Backend Architect",
                experienceLevel = "Senior",
                weeklyHours = 6,
                currentSkills = listOf("Java"),
                analysis = analysis
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("goals").document(goalId)
                    .get().await()
            }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    @Test
    fun observeGoals_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repo = SkillBridgeRepository(firestore)

        var captured: FirebaseFirestoreException? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeGoals(aliceUid).first()
            }
        } catch (e: Exception) {
            captured = e.unwrapFirestoreException()
        }
        assertNotNull(captured)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, captured?.code)
    }

    @Test
    fun brandNewProfile_startsWithZeroPointsStreakVerifiedCount_andJuniorLevel() = runBlocking {
        val uid = signInTestUser("fresh_${System.nanoTime()}@test.com")
        val repo = SkillBridgeRepository(firestore)

        val res = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(
                userId = uid,
                displayName = "Fresh Learner",
                currentRole = "Builder",
                experienceLevel = "Junior"
            )
        }
        assertTrue("upsertProfile failed: ${res.exceptionOrNull()?.stackTraceToString()}", res.isSuccess)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals(0L, profile?.points)
        assertEquals(0L, profile?.streakDays)
        assertEquals(0L, profile?.verifiedSkillCount)
        assertEquals("Junior", profile?.careerLevel)
    }

    @Test
    fun updateUserPreferences_persistsLanguageDarkThemeAiAndRoute() = runBlocking {
        val uid = signInTestUser("prefs_${System.nanoTime()}@test.com")
        val repo = SkillBridgeRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(uid, "Prefs User", "Developer", "Junior").getOrThrow()
            repo.updateUserPreferences(
                uid,
                mapOf(
                    "preferredLanguage" to "AR",
                    "darkTheme" to true,
                    "aiPersonalizationEnabled" to false,
                    "lastRoute" to "passport"
                )
            ).getOrThrow()
        }

        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { it?.preferredLanguage == "AR" }
        }
        assertEquals("AR", updated?.preferredLanguage)
        assertEquals(true, updated?.darkTheme)
        assertEquals(false, updated?.aiPersonalizationEnabled)
        assertEquals("passport", updated?.lastRoute)
    }

    @Test
    fun awardSubmissionEvaluationAndSync_isIdempotentOnApproval_andSupportsRejectionReason() = runBlocking {
        val uid = signInTestUser("eval_${System.nanoTime()}@test.com")
        val repo = SkillBridgeRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(uid, "Idempotent User", "Backend", "Junior").getOrThrow()
        }

        val approvedSubId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.submitChallengeSolution(
                userId = uid,
                challengeId = "ch_smart_city",
                challengeTitle = "Smart City Analytics Dashboard",
                companyName = "NileGrid Energy",
                category = "CHALLENGE",
                roleInTeam = "Solo",
                skillsApplied = listOf("SQL", "Kotlin"),
                repoOrDemoUrl = "https://github.com/example/smartcity",
                understandingReport = "Indexed time-series queries and verified p95 under 120ms.",
                pointsReward = 550,
                rewardAmount = "$200"
            ).getOrThrow()
        }

        val approvedSub = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSubmissions(uid).first { list -> list.any { it.id == approvedSubId } }
                .first { it.id == approvedSubId }
        }

        // First verification awards 550 points
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.awardSubmissionEvaluationAndSync(
                userId = uid,
                submission = approvedSub,
                approve = true,
                score = 92,
                feedback = "Well-structured indices."
            ).getOrThrow()
        }
        // Calling verification again for the SAME submission must remain strictly idempotent
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.awardSubmissionEvaluationAndSync(
                userId = uid,
                submission = approvedSub,
                approve = true,
                score = 95,
                feedback = "Re-verified."
            ).getOrThrow()
        }

        val profileAfterApprove = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { (it?.points ?: 0L) > 0L }
        }
        assertEquals(550L, profileAfterApprove?.points)
        assertEquals(2L, profileAfterApprove?.verifiedSkillCount)

        // Rejection stores rejectionReason and awards 0 points
        val rejectedSubId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.submitChallengeSolution(
                userId = uid,
                challengeId = "ch_re_jwt_lab",
                challengeTitle = "JWT Lab",
                companyName = "Cedars",
                category = "REVERSE_ENG",
                roleInTeam = "Solo",
                skillsApplied = listOf("JWT"),
                repoOrDemoUrl = "https://github.com/example/jwt",
                understandingReport = "Brief note.",
                pointsReward = 380,
                rewardAmount = "$100"
            ).getOrThrow()
        }
        val rejectedSub = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSubmissions(uid).first { list -> list.any { it.id == rejectedSubId } }
                .first { it.id == rejectedSubId }
        }
        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.awardSubmissionEvaluationAndSync(
                userId = uid,
                submission = rejectedSub,
                approve = false,
                score = 45,
                feedback = "Missing rotation family proof.",
                rejectionReason = "Understanding report lacks token replay test trace."
            ).getOrThrow()
        }
        val persistedRejected = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeSubmissions(uid).first { list ->
                list.any { it.id == rejectedSubId && it.status == "REJECTED" }
            }.first { it.id == rejectedSubId }
        }
        assertEquals("REJECTED", persistedRejected.status)
        assertEquals("Understanding report lacks token replay test trace.", persistedRejected.rejectionReason)
        assertEquals(0L, persistedRejected.pointsAwarded)

        val profileAfterReject = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first()
        }
        assertEquals(550L, profileAfterReject?.points)
    }

    @Test
    fun awardBadgeIfAbsent_isIdempotentAndUpdatesPointsOnce() = runBlocking {
        val uid = signInTestUser("badge_${System.nanoTime()}@test.com")
        val repo = SkillBridgeRepository(firestore)
        val firstBadge = com.example.data.seed.SkillBridgeEcosystemData.badgeDefinitions.first()

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(uid, "Badge User", "Mobile", "Junior").getOrThrow()
        }

        val firstAward = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.awardBadgeIfAbsent(uid, firstBadge).getOrThrow()
        }
        val secondAward = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.awardBadgeIfAbsent(uid, firstBadge).getOrThrow()
        }
        assertTrue(firstAward)
        assertEquals(false, secondAward)

        val badges = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBadges(uid).first { it.isNotEmpty() }
        }
        assertEquals(1, badges.size)
        assertEquals(firstBadge.id, badges.first().badgeId)

        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeProfile(uid).first { (it?.points ?: 0L) == firstBadge.pointsAwarded.toLong() }
        }
        assertEquals(firstBadge.pointsAwarded.toLong(), profile?.points)
    }

    @Test
    fun deleteUserAccountData_removesProfileAndSubcollections() = runBlocking {
        val uid = signInTestUser("del_${System.nanoTime()}@test.com")
        val repo = SkillBridgeRepository(firestore)
        val firstBadge = com.example.data.seed.SkillBridgeEcosystemData.badgeDefinitions.first()

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.upsertProfile(uid, "Delete Me", "Cloud", "Junior").getOrThrow()
            repo.awardBadgeIfAbsent(uid, firstBadge).getOrThrow()
            repo.deleteUserAccountData(uid).getOrThrow()
        }

        val profileSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("users").document(uid).get().await()
        }
        val badgesSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("users").document(uid).collection("badges").get().await()
        }
        val ledgerSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("users").document(uid).collection("pointLedger").get().await()
        }
        assertEquals(false, profileSnap.exists())
        assertTrue(badgesSnap.isEmpty)
        assertTrue(ledgerSnap.isEmpty)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_sb@test.com"
        const val BOB_EMAIL = "bob_sb@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
