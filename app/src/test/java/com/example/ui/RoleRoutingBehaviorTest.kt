package com.example.ui

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.SkillBridgeDatabase
import com.example.data.repository.SkillBridgeBackendRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RoleRoutingBehaviorTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var db: SkillBridgeDatabase
    private lateinit var backend: SkillBridgeBackendRepository
    private lateinit var auth: FirebaseAuth

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val app = if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.aistudio.skillbridge.k7m4px")
                    .setProjectId("demo-no-project")
                    .setApiKey("fake-api-key")
                    .build()
            )
        } else {
            FirebaseApp.getInstance()
        }
        auth = FirebaseAuth.getInstance(app)

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
    fun loginWithEachDemoRoleRoutesToIsolatedDashboard_andNoRoleSwitcherExists() {
        composeRule.setContent {
            AppNavigation(
                auth = auth,
                backendRepoOverride = backend
            )
        }

        composeRule.waitForIdle()
        assertTrue(
            "Mobile compact viewport must not render desktop sidebar",
            composeRule.onAllNodesWithTag("desktop_sidebar").fetchSemanticsNodes().isEmpty()
        )

        // 1. Login as Platform Admin
        signInDemoRole(fillTag = "demo_fill_admin", expectedRootTag = "admin_dashboard_root")
        assertNoRoleSwitcherPresent()
        assertTrue(composeRule.onAllNodesWithTag("desktop_sidebar").fetchSemanticsNodes().isEmpty())
        signOutRoleShell()

        // 2. Login as Company
        signInDemoRole(fillTag = "demo_fill_company", expectedRootTag = "company_dashboard_root")
        assertNoRoleSwitcherPresent()
        signOutRoleShell()

        // 3. Login as Company Director
        signInDemoRole(fillTag = "demo_fill_director", expectedRootTag = "director_dashboard_root")
        assertNoRoleSwitcherPresent()
        signOutRoleShell()

        // 4. Login as Student
        signInDemoRole(fillTag = "demo_fill_student", expectedRootTag = "student_dashboard_root")
        assertNoRoleSwitcherPresent()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1400dp-h900dp-xhdpi")
    fun desktopLayout_showsPermanentSidebarAndSyncBadgeForAllRoles_withUnifiedBackend() {
        composeRule.setContent {
            Box(modifier = Modifier.requiredSize(1366.dp, 768.dp)) {
                AppNavigation(
                    auth = auth,
                    backendRepoOverride = backend
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithTag("desktop_welcome_showcase").assertIsDisplayed()

        // 1. Platform Admin on Desktop
        signInDemoRole(fillTag = "demo_fill_admin", expectedRootTag = "admin_dashboard_root")
        composeRule.onNodeWithTag("desktop_sidebar").assertIsDisplayed()
        composeRule.onNodeWithTag("sync_status_badge").assertIsDisplayed()
        assertNoRoleSwitcherPresent()
        signOutRoleShell()

        // 2. Company on Desktop
        signInDemoRole(fillTag = "demo_fill_company", expectedRootTag = "company_dashboard_root")
        composeRule.onNodeWithTag("desktop_sidebar").assertIsDisplayed()
        composeRule.onNodeWithTag("sync_status_badge").assertIsDisplayed()
        assertNoRoleSwitcherPresent()
        signOutRoleShell()

        // 3. Company Director on Desktop
        signInDemoRole(fillTag = "demo_fill_director", expectedRootTag = "director_dashboard_root")
        composeRule.onNodeWithTag("desktop_sidebar").assertIsDisplayed()
        composeRule.onNodeWithTag("sync_status_badge").assertIsDisplayed()
        assertNoRoleSwitcherPresent()
        signOutRoleShell()

        // 4. Student on Desktop
        signInDemoRole(fillTag = "demo_fill_student", expectedRootTag = "student_dashboard_root")
        composeRule.onNodeWithTag("desktop_sidebar").assertIsDisplayed()
        composeRule.onNodeWithTag("sync_status_badge").assertIsDisplayed()
        assertNoRoleSwitcherPresent()
        signOutRoleShell()
    }

    private fun signInDemoRole(fillTag: String, expectedRootTag: String) {
        composeRule.onNodeWithTag(fillTag).performScrollTo().performClick()
        composeRule.onNodeWithTag("login_submit_button").performScrollTo().performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag(expectedRootTag).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag(expectedRootTag).assertIsDisplayed()
    }

    private fun signOutRoleShell() {
        composeRule.onNodeWithTag("role_sign_out_btn").performClick()
        composeRule.onNodeWithTag("confirm_role_sign_out_btn").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("login_submit_button").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertNoRoleSwitcherPresent() {
        val roleSwitchers = composeRule.onAllNodesWithTag("top_role_menu_btn").fetchSemanticsNodes()
        assertTrue("In-app role switcher must NEVER exist", roleSwitchers.isEmpty())
    }
}
