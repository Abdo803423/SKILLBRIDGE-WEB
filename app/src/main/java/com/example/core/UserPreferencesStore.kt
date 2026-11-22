package com.example.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.skillBridgeDataStore: DataStore<Preferences> by preferencesDataStore(name = "skillbridge_prefs")

data class PersistedAppPreferences(
    val language: AppLanguage = AppLanguage.EN,
    val darkTheme: Boolean = false,
    val lastTab: String = "home",
    val lastChallengeId: String = "",
    val aiPersonalizationEnabled: Boolean = true,
    val aiContextSharingEnabled: Boolean = true,
    val guestSavedChallengeIds: Set<String> = emptySet(),
    val guestDomain: String = "Backend & Cloud",
    val guestPreviewActive: Boolean = false,
    val activeAccountId: String = "",
    val backendToken: String = "",
    val serverBaseUrl: String = BuildConfig.SKILLBRIDGE_API_BASE_URL
)

class UserPreferencesStore(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.applicationContext.skillBridgeDataStore)

    val preferencesFlow: Flow<PersistedAppPreferences> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val lang = if (prefs[KEY_LANGUAGE] == "AR") AppLanguage.AR else AppLanguage.EN
            val storedUrl = prefs[KEY_SERVER_BASE_URL]?.trim().orEmpty()
            PersistedAppPreferences(
                language = lang,
                darkTheme = prefs[KEY_DARK_THEME] ?: false,
                lastTab = prefs[KEY_LAST_TAB] ?: "home",
                lastChallengeId = prefs[KEY_LAST_CHALLENGE_ID] ?: "",
                aiPersonalizationEnabled = prefs[KEY_AI_PERSONALIZATION] ?: true,
                aiContextSharingEnabled = prefs[KEY_AI_CONTEXT_SHARING] ?: true,
                guestSavedChallengeIds = prefs[KEY_GUEST_SAVED_CHALLENGES] ?: emptySet(),
                guestDomain = prefs[KEY_GUEST_DOMAIN] ?: "Backend & Cloud",
                guestPreviewActive = prefs[KEY_GUEST_PREVIEW_ACTIVE] ?: false,
                activeAccountId = prefs[KEY_ACTIVE_ACCOUNT_ID] ?: "",
                backendToken = prefs[KEY_BACKEND_TOKEN] ?: "",
                serverBaseUrl = storedUrl.ifBlank { BuildConfig.SKILLBRIDGE_API_BASE_URL }
            )
        }

    suspend fun getBackendToken(): String = preferencesFlow.first().backendToken

    suspend fun getServerBaseUrl(): String = preferencesFlow.first().serverBaseUrl

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language.name
        }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_THEME] = enabled
        }
    }

    suspend fun setLastTab(route: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_TAB] = route
        }
    }

    suspend fun setLastChallengeId(challengeId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_CHALLENGE_ID] = challengeId
        }
    }

    suspend fun setAiPreferences(personalization: Boolean, contextSharing: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_AI_PERSONALIZATION] = personalization
            prefs[KEY_AI_CONTEXT_SHARING] = contextSharing
        }
    }

    suspend fun setGuestPreviewActive(active: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_GUEST_PREVIEW_ACTIVE] = active
        }
    }

    suspend fun setActiveAccountId(accountId: String) {
        dataStore.edit { prefs ->
            prefs[KEY_ACTIVE_ACCOUNT_ID] = accountId
        }
    }

    suspend fun setBackendToken(token: String) {
        dataStore.edit { prefs ->
            prefs[KEY_BACKEND_TOKEN] = token
        }
    }

    suspend fun setServerBaseUrl(url: String) {
        val clean = url.trim().trimEnd('/')
        if (clean.startsWith("https://") || clean.startsWith("http://")) {
            dataStore.edit { prefs ->
                prefs[KEY_SERVER_BASE_URL] = clean
            }
        }
    }

    suspend fun setGuestSavedChallenges(ids: Set<String>) {
        dataStore.edit { prefs ->
            prefs[KEY_GUEST_SAVED_CHALLENGES] = ids
        }
    }

    suspend fun setGuestDomain(domain: String) {
        dataStore.edit { prefs ->
            prefs[KEY_GUEST_DOMAIN] = domain
        }
    }

    suspend fun clearSessionScopedStatePreserveLanguage() {
        dataStore.edit { prefs ->
            val savedLang = prefs[KEY_LANGUAGE]
            val savedDark = prefs[KEY_DARK_THEME]
            val savedUrl = prefs[KEY_SERVER_BASE_URL]
            prefs.clear()
            if (savedLang != null) prefs[KEY_LANGUAGE] = savedLang
            if (savedDark != null) prefs[KEY_DARK_THEME] = savedDark
            if (savedUrl != null) prefs[KEY_SERVER_BASE_URL] = savedUrl
        }
    }

    private companion object {
        val KEY_LANGUAGE = stringPreferencesKey("preferred_language")
        val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        val KEY_LAST_TAB = stringPreferencesKey("last_tab")
        val KEY_LAST_CHALLENGE_ID = stringPreferencesKey("last_challenge_id")
        val KEY_AI_PERSONALIZATION = booleanPreferencesKey("ai_personalization")
        val KEY_AI_CONTEXT_SHARING = booleanPreferencesKey("ai_context_sharing")
        val KEY_GUEST_SAVED_CHALLENGES = stringSetPreferencesKey("guest_saved_challenges")
        val KEY_GUEST_DOMAIN = stringPreferencesKey("guest_domain")
        val KEY_GUEST_PREVIEW_ACTIVE = booleanPreferencesKey("guest_preview_active")
        val KEY_ACTIVE_ACCOUNT_ID = stringPreferencesKey("active_account_id")
        val KEY_BACKEND_TOKEN = stringPreferencesKey("backend_token")
        val KEY_SERVER_BASE_URL = stringPreferencesKey("server_base_url")
    }
}
