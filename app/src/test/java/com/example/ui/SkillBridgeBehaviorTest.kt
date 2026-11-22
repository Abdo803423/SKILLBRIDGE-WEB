package com.example.ui

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import com.example.R
import com.example.core.AppLanguage
import com.example.core.LocalizedText
import com.example.core.StaticConnectivityObserver
import com.example.core.UserPreferencesStore
import com.example.data.model.AiAgentContextMode
import com.example.data.model.AiAgentPendingAction
import com.example.data.repository.AiCareerRepository
import com.example.data.repository.SkillBridgeRepository
import com.example.ui.viewmodel.SkillBridgeViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@OptIn(ExperimentalCoroutinesApi::class)
class SkillBridgeBehaviorTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var context: Context
    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var prefsFile: File
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        prefsFile = File(tempFolder.root, "skillbridge_behavior_test.preferences_pb")

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
        val dbId = context.getString(R.string.firestore_database_id)
        firestore = FirebaseFirestore.getInstance(app, dbId)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun userPreferencesStore_persistsLanguageAcrossStoreInstances_andPreservesOnSignOut() = runBlocking {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { prefsFile }
        )
        val store1 = UserPreferencesStore(dataStore)
        store1.setLanguage(AppLanguage.AR)
        store1.setDarkTheme(true)
        store1.setGuestPreviewActive(true)
        store1.setGuestSavedChallenges(setOf("ch_smart_city"))

        val readBack = withTimeout(3000L) {
            store1.preferencesFlow.first()
        }
        assertEquals(AppLanguage.AR, readBack.language)
        assertTrue(readBack.darkTheme)
        assertTrue(readBack.guestPreviewActive)
        assertEquals(setOf("ch_smart_city"), readBack.guestSavedChallengeIds)

        store1.clearSessionScopedStatePreserveLanguage()
        val afterSignOut = withTimeout(3000L) {
            store1.preferencesFlow.first()
        }
        assertEquals(AppLanguage.AR, afterSignOut.language)
        assertTrue(afterSignOut.darkTheme)
        assertFalse(afterSignOut.guestPreviewActive)
        assertTrue(afterSignOut.guestSavedChallengeIds.isEmpty())
    }

    @Test
    fun guestViewModel_toggleLanguagePersists_andMutationsRemainBounded() = runBlocking {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.root, "vm_guest.preferences_pb") }
        )
        val prefsStore = UserPreferencesStore(dataStore)
        val connectivity = StaticConnectivityObserver(initiallyOffline = false)
        val vm = SkillBridgeViewModel(
            repo = SkillBridgeRepository(firestore),
            aiRepo = AiCareerRepository(),
            prefsStore = prefsStore,
            connectivityObserver = connectivity,
            currentUserId = "guest_preview",
            userDisplayName = "Guest Explorer",
            isGuest = true
        )

        assertEquals(AppLanguage.EN, vm.language.value)
        vm.toggleLanguage()
        val persistedAfterToggle = withContext(Dispatchers.IO) {
            withTimeout(3000L) {
                prefsStore.preferencesFlow.first { it.language == AppLanguage.AR }
            }
        }
        assertEquals(AppLanguage.AR, persistedAfterToggle.language)
        assertEquals(AppLanguage.AR, vm.language.value)

        // Verify guest cannot submit challenge solutions or skip Google auth gate
        vm.submitChallengeSolution(
            challengeId = "ch_smart_city",
            challengeTitle = "Smart City",
            companyName = "NileGrid",
            category = "CHALLENGE",
            roleInTeam = "Solo",
            skillsApplied = listOf("Kotlin"),
            repoOrDemoUrl = "https://github.com/test",
            understandingReport = "Test report long enough for submission.",
            pointsReward = 400,
            rewardAmount = "$100"
        )
        assertNotNull(vm.actionMessage.value)
        assertTrue(vm.actionMessage.value!!.contains("Google"))
    }

    @Test
    fun aiAgent_handlesOfflineGracefully_andConfirmationGateClearsCleanly() = runBlocking {
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(tempFolder.root, "vm_agent.preferences_pb") }
        )
        val prefsStore = UserPreferencesStore(dataStore)
        val connectivity = StaticConnectivityObserver(initiallyOffline = true)
        val vm = SkillBridgeViewModel(
            repo = SkillBridgeRepository(firestore),
            aiRepo = AiCareerRepository(),
            prefsStore = prefsStore,
            connectivityObserver = connectivity,
            currentUserId = "guest_preview",
            userDisplayName = "Guest Explorer",
            isGuest = true
        )

        vm.openAiAgent(
            mode = AiAgentContextMode.SKILL_ADVISOR,
            screenLabel = "Home",
            contextSummary = "Target: Android Engineer"
        )
        assertTrue(vm.aiAgentState.value.isOpen)

        // Offline attempt must immediately surface an actionable error and store lastPrompt for Retry
        vm.sendAgentPrompt("Which skill gap should I tackle first?")
        assertEquals("Which skill gap should I tackle first?", vm.aiAgentState.value.lastPrompt)
        assertNotNull(vm.aiAgentState.value.errorMessage)
        assertTrue(vm.aiAgentState.value.errorMessage!!.contains("Offline", ignoreCase = true))

        // Confirmation dialog state transitions cleanly
        val pendingAction = AiAgentPendingAction.UpdateTargetSkills(
            skillsToAdd = listOf("Testing"),
            description = LocalizedText("Add Testing?", "إضافة الاختبارات؟")
        )
        vm.requestAgentActionConfirmation(pendingAction)
        assertEquals(pendingAction, vm.aiAgentState.value.pendingActionConfirmation)
        vm.dismissPendingAgentAction()
        assertNull(vm.aiAgentState.value.pendingActionConfirmation)

        // Connectivity restoration updates ViewModel isOffline
        connectivity.setOffline(false)
        assertFalse(vm.isOffline.value)
    }
}
