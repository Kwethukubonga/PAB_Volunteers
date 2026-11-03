package com.kantu.pab_volunteers.ui.auth

import android.app.Activity
import android.content.Context
import androidx.annotation.StringRes
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CredentialOption
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialProviderConfigurationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.kantu.pab_volunteers.R
import com.kantu.pab_volunteers.utils.AppError
import kotlinx.coroutines.CancellationException

class GoogleSignInError(
    @StringRes messageRes: Int,
    args: List<Any> = emptyList(),
    val isCancelled: Boolean = false
) : AppError(messageRes, args)

/**
 * Google sign-in through Credential Manager (K16), Android's current sign-in API, which replaces
 * the deprecated GoogleSignIn client. Each call returns a Firebase credential for AuthViewModel or
 * for confirming it's really them before deleting an account.
 */
object GoogleSignIn {

    /** "Continue with Google": always shows the account picker, so anyone can switch account. */
    suspend fun credential(activity: Activity): Result<AuthCredential> =
        request(activity, GetSignInWithGoogleOption.Builder(clientId(activity)).build())

    /**
     * Fingerprint sign-in for a Google account: back in to [email], the account set up on this
     * phone, without the picker where Google allows it. A token for any other account is refused.
     */
    suspend fun credentialFor(activity: Activity, email: String): Result<AuthCredential> =
        request(
            activity,
            GetGoogleIdOption.Builder()
                .setServerClientId(clientId(activity))
                .setFilterByAuthorizedAccounts(true)
                .setAutoSelectEnabled(true)
                .build(),
            expectedEmail = email
        )

    /** Signing out forgets the account Google would otherwise pick by itself next time. */
    suspend fun signOut(context: Context) {
        runCatching { CredentialManager.create(context).clearCredentialState(ClearCredentialStateRequest()) }
    }

    private suspend fun request(
        activity: Activity,
        option: CredentialOption,
        expectedEmail: String? = null
    ): Result<AuthCredential> {
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        return try {
            val credential = CredentialManager.create(activity).getCredential(activity, request).credential
            if (credential !is CustomCredential ||
                credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                return Result.failure(GoogleSignInError(R.string.error_google_incomplete))
            }
            val google = GoogleIdTokenCredential.createFrom(credential.data)
            if (expectedEmail != null && !google.id.equals(expectedEmail, ignoreCase = true)) {
                return Result.failure(GoogleSignInError(R.string.error_google_wrong_account, listOf(expectedEmail)))
            }
            Result.success(GoogleAuthProvider.getCredential(google.idToken, null))
        } catch (e: CancellationException) {
            throw e
        } catch (e: GetCredentialCancellationException) {
            Result.failure(GoogleSignInError(R.string.error_google_cancelled, isCancelled = true))
        } catch (e: NoCredentialException) {
            Result.failure(GoogleSignInError(R.string.error_google_no_account))
        } catch (e: GetCredentialProviderConfigurationException) {
            Result.failure(GoogleSignInError(R.string.error_google_not_registered))
        } catch (e: GetCredentialException) {
            Result.failure(GoogleSignInError(R.string.error_google_incomplete))
        } catch (e: GoogleIdTokenParsingException) {
            Result.failure(GoogleSignInError(R.string.error_google_incomplete))
        }
    }

    private fun clientId(context: Context) = context.getString(R.string.default_web_client_id)
}
