package com.example.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AccountRole
import com.example.data.local.SkillBridgeDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SkillBridgeBackendRbacTest {

    private lateinit var db: SkillBridgeDatabase
    private lateinit var backend: SkillBridgeBackendRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SkillBridgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        backend = SkillBridgeBackendRepository(db)
        runBlocking { backend.ensureSeeded() }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun fourDemoAccountsAuthenticateWithSaltedPbkdf2Hashes_andWrongPasswordsFail() = runBlocking {
        val expected = listOf(
            Triple("admin_demo", "Admin@123", AccountRole.PLATFORM_ADMIN),
            Triple("student_demo", "Student@123", AccountRole.STUDENT),
            Triple("company_demo", "Company@123", AccountRole.COMPANY),
            Triple("director_demo", "Director@123", AccountRole.COMPANY_DIRECTOR)
        )

        val dao = db.skillBridgeDao()
        for ((loginId, rawPassword, expectedRole) in expected) {
            val stored = dao.getAccountByLoginId(loginId)
            assertNotNull("Seeded account $loginId must exist", stored)
            assertNotEquals("Password must NEVER be stored in plaintext", rawPassword, stored!!.passwordHash)
            assertTrue("Salted PBKDF2 salt must be non-empty", stored.passwordSalt.isNotBlank())

            val ok = backend.authenticateWithCredentials(loginId, rawPassword)
            assertTrue("Valid login for $loginId must succeed", ok.isSuccess)
            assertEquals(expectedRole, ok.getOrThrow().role)

            val bad = backend.authenticateWithCredentials(loginId, "WrongPass@999")
            assertTrue("Wrong password for $loginId must fail", bad.isFailure)
            assertTrue(bad.exceptionOrNull() is BackendAuthorizationException)
        }
    }

    @Test
    fun strictRbacAndDirectorAssignmentIsolationAreEnforced() = runBlocking {
        val admin = backend.authenticateWithCredentials("admin_demo", "Admin@123").getOrThrow()
        val student = backend.authenticateWithCredentials("student_demo", "Student@123").getOrThrow()
        val company = backend.authenticateWithCredentials("company_demo", "Company@123").getOrThrow()
        val director = backend.authenticateWithCredentials("director_demo", "Director@123").getOrThrow()

        // 1. Student cannot create brief, approve brief, approve company, decide application, or evaluate submission
        assertTrue(
            backend.createProjectBrief(
                student, "Unauthorized Brief", "Summary for unauthorized test",
                "Data & AI", "Junior", listOf("Kotlin"), emptyList(), emptyList(),
                "May 1", 100, "Points", "", true
            ).exceptionOrNull() is BackendAuthorizationException
        )
        assertTrue(
            backend.reviewProjectBrief(student, "brief_pending_edge", true, "hack")
                .exceptionOrNull() is BackendAuthorizationException
        )
        assertTrue(
            backend.reviewCompanyVerification(student, "comp_oasisfin", true, "TRUSTED", "hack")
                .exceptionOrNull() is BackendAuthorizationException
        )
        assertTrue(
            backend.evaluateSubmission(student, "sub_lina_smart_city", 100, "self verify", "VERIFIED", "")
                .exceptionOrNull() is BackendAuthorizationException
        )

        // 2. Company cannot self-approve its own brief or evaluate submissions
        assertTrue(
            backend.reviewProjectBrief(company, "brief_pending_edge", true, "self approve")
                .exceptionOrNull() is BackendAuthorizationException
        )
        assertTrue(
            backend.evaluateSubmission(company, "sub_lina_smart_city", 95, "company eval", "VERIFIED", "")
                .exceptionOrNull() is BackendAuthorizationException
        )

        // 3. Director isolation: director_demo belongs to NileGrid and CANNOT see or manage OasisFin's brief_oasisfin_vault
        val directorBriefIds = backend.observeDirectorBriefs(director.accountId).first().map { it.id }
        assertTrue("brief_smart_city" in directorBriefIds)
        assertFalse("director_demo must NOT see OasisFin brief", "brief_oasisfin_vault" in directorBriefIds)

        val unownedTaskAttempt = backend.createProjectTask(
            director, "brief_oasisfin_vault", student.accountId, "Injected task", "Jan 1"
        )
        assertTrue(unownedTaskAttempt.exceptionOrNull() is BackendAuthorizationException)

        // 4. Unaccepted student cannot submit work on brief_gateway_ledger
        val unacceptedSubmit = backend.submitProjectWork(
            student, "brief_gateway_ledger", "https://github.com/demo", "Attempting work before acceptance", listOf("Kotlin")
        )
        assertTrue(unacceptedSubmit.exceptionOrNull() is BackendAuthorizationException)

        // 5. Suspended user cannot authenticate
        assertTrue(backend.setUserActiveStatus(admin, "acc_student_sara", false).isSuccess)
        val suspendedLogin = backend.authenticateWithCredentials("sara_nabil", "DemoPass@2026")
        assertTrue(suspendedLogin.exceptionOrNull() is BackendAuthorizationException)
    }

    @Test
    fun endToEndWorkflow_companyBrief_adminApprove_studentApply_companyAccept_directorVerify_updatesPassport() = runBlocking {
        val admin = backend.authenticateWithCredentials("admin_demo", "Admin@123").getOrThrow()
        val student = backend.authenticateWithCredentials("student_demo", "Student@123").getOrThrow()
        val company = backend.authenticateWithCredentials("company_demo", "Company@123").getOrThrow()
        val director = backend.authenticateWithCredentials("director_demo", "Director@123").getOrThrow()

        val initialAccount = backend.observeAccount(student.accountId).first()!!
        val initialPoints = initialAccount.points
        val initialVerified = initialAccount.verifiedCount

        // Step 1: Company creates brief -> PENDING_APPROVAL (not published yet)
        val briefId = backend.createProjectBrief(
            session = company,
            title = "Realtime Fraud Detector Pipeline",
            summary = "Implement deterministic transaction anomaly scoring with Kotlin & SQL.",
            domain = "Data & AI",
            level = "Mid-level",
            skills = listOf("Kotlin", "SQL", "Anomaly Detection"),
            rubric = listOf("Precision (50%)", "Tests (50%)"),
            deliverables = listOf("Git Repo", "Benchmarks"),
            deadline = "Apr 20, 2027",
            pointsReward = 400,
            prizePool = "$500",
            assignedDirectorId = director.accountId,
            isTeamAllowed = true
        ).getOrThrow()

        val publishedBeforeApproval = backend.observePublishedBriefs().first().map { it.id }
        assertFalse("Unapproved brief must NOT appear to students", briefId in publishedBeforeApproval)

        // Step 2: Admin approves brief -> becomes visible to students
        assertTrue(backend.reviewProjectBrief(admin, briefId, true, "Approved for Cohort 2027").isSuccess)
        val publishedAfterApproval = backend.observePublishedBriefs().first().map { it.id }
        assertTrue("Approved brief must appear to students", briefId in publishedAfterApproval)

        // Step 3: Student applies
        val appId = backend.applyToProjectBrief(
            student, briefId, "Implemented production anomaly detectors in Kotlin.", "ML Pipeline Engineer"
        ).getOrThrow()

        // Step 4: Company accepts application -> brief activates & kickoff task assigned
        assertTrue(backend.decideApplication(company, appId, "ACCEPTED", "Welcome to the cohort").isSuccess)
        val studentTasksAfterAccept = backend.observeStudentTasks(student.accountId).first()
            .filter { it.briefId == briefId }
        assertTrue("Initial kickoff task should be created on accept", studentTasksAfterAccept.isNotEmpty())

        // Step 5: Director assigns milestone task -> Student advances status
        val customTaskId = backend.createProjectTask(
            director, briefId, student.accountId, "Deliver deterministic threshold unit tests", "Apr 10, 2027"
        ).getOrThrow()
        assertTrue(backend.updateStudentTaskStatus(student, customTaskId, "IN_PROGRESS").isSuccess)
        assertTrue(backend.updateStudentTaskStatus(student, customTaskId, "DONE").isSuccess)
        val doneTask = backend.observeDirectorTasks(director.accountId).first().first { it.id == customTaskId }
        assertEquals("DONE", doneTask.status)

        // Step 6: Student submits deliverable -> Director sees it, unrelated director_tariq does NOT
        val subId = backend.submitProjectWork(
            session = student,
            briefId = briefId,
            repoOrDemoUrl = "https://github.com/lina/fraud-pipeline",
            understandingReport = "Implemented sliding window z-score detection with deterministic replay tests.",
            skillsApplied = listOf("Kotlin", "SQL", "Anomaly Detection")
        ).getOrThrow()

        val directorSubs = backend.observeDirectorSubmissions(director.accountId).first().map { it.id }
        val otherDirectorSubs = backend.observeDirectorSubmissions("acc_director_tariq").first().map { it.id }
        assertTrue(subId in directorSubs)
        assertFalse(subId in otherDirectorSubs)

        // Step 7: Director evaluates & verifies submission -> Student points, verifiedCount, skills & notifications update
        assertTrue(
            backend.evaluateSubmission(
                director, subId, 95, "Outstanding deterministic pipeline & test coverage.", "VERIFIED", ""
            ).isSuccess
        )

        val updatedStudent = backend.observeAccount(student.accountId).first()!!
        assertEquals(initialPoints + 400, updatedStudent.points)
        assertEquals(initialVerified + 1, updatedStudent.verifiedCount)
        assertTrue(updatedStudent.skillsCsv.contains("Anomaly Detection"))

        val verifiedSub = backend.observeStudentSubmissions(student.accountId).first().first { it.id == subId }
        assertEquals("VERIFIED", verifiedSub.status)
        assertEquals(95, verifiedSub.score)
        assertTrue(verifiedSub.verificationCode.startsWith("SB-VER-"))

        val studentNotifs = backend.observeNotifications(student.accountId).first()
        assertTrue(studentNotifs.any { it.titleEn.contains("Passport Evidence Verified") })
    }

    @Test
    fun crossDeviceSync_mobileAndDesktopShareDatabaseAndEnforceSessionRevocation() = runBlocking {
        val student = backend.authenticateWithCredentials("student_demo", "Student@123").getOrThrow()
        val company = backend.authenticateWithCredentials("company_demo", "Company@123").getOrThrow()
        val director = backend.authenticateWithCredentials("director_demo", "Director@123").getOrThrow()

        val initialAccount = backend.observeAccount(student.accountId).first()!!

        // 1. Register simultaneous Mobile and Desktop/Laptop sessions for the same Student account
        val mobileReg = backend.registerDeviceSession(
            accountId = student.accountId,
            sessionId = "sess_student_mobile",
            deviceType = "MOBILE",
            deviceLabel = "Pixel Phone (411dp)",
            windowWidthDp = 411
        )
        val desktopReg = backend.registerDeviceSession(
            accountId = student.accountId,
            sessionId = "sess_student_desktop",
            deviceType = "DESKTOP_LAPTOP",
            deviceLabel = "Laptop Workstation (1366dp)",
            windowWidthDp = 1366
        )
        assertTrue(mobileReg.isSuccess)
        assertTrue(desktopReg.isSuccess)

        val sessions = backend.observeDeviceSessions(student.accountId).first()
        assertEquals(2, sessions.size)
        assertTrue(sessions.any { it.id == "sess_student_mobile" && it.deviceType == "MOBILE" && !it.revoked })
        assertTrue(sessions.any { it.id == "sess_student_desktop" && it.deviceType == "DESKTOP_LAPTOP" && !it.revoked })

        // 2. Cross-device live collaboration against the single shared database:
        // Student applies on Mobile -> Company accepts on Desktop -> Student submits on Desktop -> Director verifies on Desktop
        val appId = backend.applyToProjectBrief(
            session = student,
            briefId = "brief_gateway_ledger",
            coverNote = "Applied from Mobile phone during commute.",
            roleRequested = "Backend Engineer"
        ).getOrThrow()

        assertTrue(backend.decideApplication(company, appId, "ACCEPTED", "Approved from Company Desktop").isSuccess)

        val subId = backend.submitProjectWork(
            session = student,
            briefId = "brief_gateway_ledger",
            repoOrDemoUrl = "https://github.com/lina/grid-settlement",
            understandingReport = "Delivered idempotent settlement ledger from Laptop workstation.",
            skillsApplied = listOf("Kotlin", "Distributed Systems")
        ).getOrThrow()

        assertTrue(
            backend.evaluateSubmission(
                director,
                subId,
                96,
                "Verified across mobile & desktop unified session.",
                "VERIFIED",
                ""
            ).isSuccess
        )

        val syncedStudent = backend.observeAccount(student.accountId).first()!!
        assertEquals(initialAccount.points + 350, syncedStudent.points)
        assertEquals(initialAccount.verifiedCount + 1, syncedStudent.verifiedCount)
        assertEquals("VERIFIED", backend.observeStudentSubmissions(student.accountId).first().first { it.id == subId }.status)
        assertTrue(backend.observeNotifications(student.accountId).first().any { !it.isRead && it.titleEn.contains("Passport Evidence Verified") })

        // 3. Cross-account session revocation is strictly forbidden, while self-revocation succeeds
        val crossRevoke = backend.revokeDeviceSession(company.accountId, "sess_student_desktop")
        assertTrue(crossRevoke.exceptionOrNull() is BackendAuthorizationException)

        assertTrue(backend.revokeDeviceSession(student.accountId, "sess_student_mobile").isSuccess)
        val revokedSession = backend.observeDeviceSession("sess_student_mobile").first()
        assertNotNull(revokedSession)
        assertTrue(revokedSession!!.revoked)
        assertFalse(backend.observeDeviceSession("sess_student_desktop").first()!!.revoked)
    }
}
