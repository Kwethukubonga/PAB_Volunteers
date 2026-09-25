package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.kantu.pab_volunteers.R

class GoogleSignInError(val statusCode: Int, message: String) : Exception(message) {
    val isCancelled: Boolean get() = statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED
}

class GoogleSignInHelper(private val activity: Activity) {

    private val client: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(activity, options)
    }

    val signInIntent: Intent get() = client.signInIntent

    // Turns the picker result into a Firebase credential.
    fun credentialFrom(data: Intent?): Result<AuthCredential> {
        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
            val idToken = account.idToken
                ?: return Result.failure(IllegalStateException("Google did not return an ID token"))
            Result.success(GoogleAuthProvider.getCredential(idToken, null))
        } catch (e: ApiException) {
            Result.failure(GoogleSignInError(e.statusCode, describe(e.statusCode)))
        }
    }

    private fun describe(statusCode: Int): String = when (statusCode) {
        CommonStatusCodes.DEVELOPER_ERROR ->
            "This build is not registered in Firebase. Add this machine's debug SHA-1 fingerprint."
        CommonStatusCodes.NETWORK_ERROR -> "No connection. Check your internet and try again."
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Sign in cancelled"
        else -> "Google sign in failed (code $statusCode)"
    }

    fun signOut() {
        client.signOut()
    }
}
