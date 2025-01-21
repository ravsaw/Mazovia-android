package pl.edu.mazovia.mazovia.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.auth0.android.jwt.JWT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File
import java.security.KeyStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TokenService {
    private const val DATASTORE_FILE_NAME = "pl_emazovia_token_service.preferences_pb"
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"

    // Preference keys
    private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
    private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
    private val SERVER_CODE_KEY = stringPreferencesKey("server_code")
    private val TFA_CODE_KEY = stringPreferencesKey("tfa_code")

    // KeyStore instance
    private val keyStore: KeyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
        load(null)
    }

    private var store: DataStore<Preferences>? = null

    // DataStore creation method
    private fun getDataStore(context: Context): DataStore<Preferences> {
        if (store == null) {
            store = PreferenceDataStoreFactory.create(
                corruptionHandler = null,
                migrations = emptyList(),
                produceFile = { File(context.applicationContext.filesDir, DATASTORE_FILE_NAME) }
            )
        }
        return store!!
    }

    /**
     * Save access and refresh tokens
     */
    suspend fun saveToken(context: Context, accessToken: String, refreshToken: String) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences[ACCESS_TOKEN_KEY] = accessToken
                preferences[REFRESH_TOKEN_KEY] = refreshToken
            }
        }

    /**
     * Save server code
     */
    suspend fun saveServerCode(context: Context, serverCode: String) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences[SERVER_CODE_KEY] = serverCode
            }
        }

    /**
     * Save two-factor authentication code
     */
    suspend fun saveTfaCode(context: Context, tfaCode: String) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences[TFA_CODE_KEY] = tfaCode
            }
        }

    /**
     * Remove tokens
     */
    suspend fun removeToken(context: Context) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences.remove(ACCESS_TOKEN_KEY)
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }

    /**
     * Remove server code
     */
    suspend fun removeServerCode(context: Context) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences.remove(SERVER_CODE_KEY)
            }
        }

    /**
     * Remove two-factor authentication code
     */
    suspend fun removeTfaCode(context: Context) =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.edit { preferences ->
                preferences.remove(TFA_CODE_KEY)
            }
        }

    /**
     * Check if refresh token exists
     */
    suspend fun checkRefreshToken(context: Context): Boolean =
        getRefreshToken(context) != null

    /**
     * Check if access token exists
     */
    suspend fun checkAccessToken(context: Context): Boolean =
        getAccessToken(context) != null

    /**
     * Get access token
     */
    suspend fun getAccessToken(context: Context): String? =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.data
                .map { preferences -> preferences[ACCESS_TOKEN_KEY] }
                .first()
        }

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(context: Context): String? =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.data
                .map { preferences -> preferences[REFRESH_TOKEN_KEY] }
                .first()
        }

    fun isExpired(token: String): Boolean {
        return JWT(token).isExpired(60)
    }

    /**
     * Get server code
     */
    suspend fun getServerCode(context: Context): String? =
        withContext(Dispatchers.IO) {
            val dataStore = getDataStore(context)
            dataStore.data
                .map { preferences -> preferences[SERVER_CODE_KEY] }
                .first()
        }
}