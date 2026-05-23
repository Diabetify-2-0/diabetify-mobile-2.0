package com.itb.diabetify.data.manager

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.itb.diabetify.domain.manager.TokenManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.security.GeneralSecurityException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TokenManager {
    private val masterKeyAlias = MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = createEncryptedPreferences()

    override suspend fun saveToken(token: String) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    override suspend fun getToken(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_AUTH_TOKEN, null)
    }

    override suspend fun clearToken() = withContext(Dispatchers.IO) {
        sharedPreferences.edit().remove(KEY_AUTH_TOKEN).apply()
    }

    override suspend fun isLoggedIn(): Boolean = withContext(Dispatchers.IO) {
        !getToken().isNullOrEmpty()
    }

    private fun createEncryptedPreferences() =
        try {
            buildEncryptedPreferences()
        } catch (exception: Exception) {
            if (!exception.isRecoverableSecurityFailure()) throw exception

            Log.w(TAG, "Encrypted token storage is invalid. Clearing local auth token store.", exception)
            clearTokenPreferences()
            buildEncryptedPreferences()
        }

    private fun buildEncryptedPreferences() = EncryptedSharedPreferences.create(
        context,
        PREF_NAME,
        masterKeyAlias,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun Exception.isRecoverableSecurityFailure(): Boolean {
        if (this is GeneralSecurityException || this is IOException) return true
        return cause is GeneralSecurityException || cause is IOException
    }

    private fun clearTokenPreferences() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.deleteSharedPreferences(PREF_NAME)
        } else {
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREF_NAME = "auth_token"
        private const val KEY_AUTH_TOKEN = "auth_token"
    }
}
