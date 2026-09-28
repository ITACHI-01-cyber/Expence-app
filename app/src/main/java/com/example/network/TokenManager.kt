package com.example.network

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREFS_NAME = "expencetrack_secure_prefs"
    private const val KEY_JWT_TOKEN = "jwt_access_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"

    @Volatile
    private var prefs: SharedPreferences? = null

    @Volatile
    private var cachedToken: String? = null

    fun init(context: Context) {
        if (prefs == null) {
            synchronized(this) {
                if (prefs == null) {
                    val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs = p
                    cachedToken = p.getString(KEY_JWT_TOKEN, null)
                }
            }
        }
    }

    fun saveToken(token: String?) {
        cachedToken = token
        prefs?.edit()?.putString(KEY_JWT_TOKEN, token)?.apply()
    }

    fun getToken(): String? {
        if (cachedToken != null) return cachedToken
        cachedToken = prefs?.getString(KEY_JWT_TOKEN, null)
        return cachedToken
    }

    fun saveUserInfo(id: String?, name: String?, email: String?) {
        prefs?.edit()
            ?.putString(KEY_USER_ID, id)
            ?.putString(KEY_USER_NAME, name)
            ?.putString(KEY_USER_EMAIL, email)
            ?.apply()
    }

    fun getUserId(): String? = prefs?.getString(KEY_USER_ID, null)
    fun getUserName(): String? = prefs?.getString(KEY_USER_NAME, null)
    fun getUserEmail(): String? = prefs?.getString(KEY_USER_EMAIL, null)

    fun clear() {
        cachedToken = null
        prefs?.edit()?.clear()?.apply()
    }

    fun isLoggedIn(): Boolean {
        val t = getToken()
        return !t.isNullOrBlank()
    }
}
