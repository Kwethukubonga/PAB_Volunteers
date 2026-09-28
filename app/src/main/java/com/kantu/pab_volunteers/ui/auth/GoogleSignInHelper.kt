package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.annotation.StringRes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.utils.AppError

class GoogleSignInError(
    val statusCode: Int,
    @StringRes messageRes: Int,
    args: List<Any> = emptyList()
) : AppError(messageRes, args) {
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
                ?: return Result.failure(AppError(R.string.error_google_incomplete))
            Result.success(GoogleAuthProvider.getCredential(idToken, null))
        } catch (e: ApiException) {
            Result.failure(errorFor(e.statusCode))
        }
    }

    private fun errorFor(statusCode: Int): GoogleSignInError = when (statusCode) {
        CommonStatusCodes.DEVELOPER_ERROR ->
            GoogleSignInError(statusCode, R.string.error_google_not_registered)
        CommonStatusCodes.NETWORK_ERROR ->
            GoogleSignInError(statusCode, R.string.error_no_connection)
        GoogleSignInStatusCodes.SIGN_IN_CANCELLED ->
            GoogleSignInError(statusCode, R.string.error_google_cancelled)
        else ->
            GoogleSignInError(statusCode, R.string.error_google_failed, listOf(statusCode))
    }

    fun signOut() {
        client.signOut()
    }
}
