package com.kantu.pab_volunteers.utils

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kantu.pab_volunteers.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

/**
 * Every failure in the app is worded here. A wrong mapping is how a volunteer ends up
 * staring at "Something went wrong" when the real problem was that they were offline.
 */
class ErrorMessagesTest {

    private fun firestore(code: FirebaseFirestoreException.Code) =
        FirebaseFirestoreException("from the server", code)

    private fun resOf(text: UiText): Int = (text as UiText.Res).id

    @Test
    fun `losing the connection says so, rather than something generic`() {
        val text = ErrorMessages.textFor(FirebaseNetworkException("no network"))
        assertEquals(R.string.error_no_connection, resOf(text))
        assertEquals(ErrorMessages.OFFLINE, text)
    }

    @Test
    fun `an unreachable server reads as being offline`() {
        val text = ErrorMessages.textFor(firestore(FirebaseFirestoreException.Code.UNAVAILABLE))
        assertEquals(R.string.error_no_connection, resOf(text))
    }

    @Test
    fun `a timeout reads as being offline`() {
        val text =
            ErrorMessages.textFor(firestore(FirebaseFirestoreException.Code.DEADLINE_EXCEEDED))
        assertEquals(R.string.error_no_connection, resOf(text))
    }

    @Test
    fun `being refused by the security rules is named plainly`() {
        val text =
            ErrorMessages.textFor(firestore(FirebaseFirestoreException.Code.PERMISSION_DENIED))
        assertEquals(R.string.error_permission_denied, resOf(text))
    }

    @Test
    fun `a stale session tells the person to sign in again`() {
        val text =
            ErrorMessages.textFor(firestore(FirebaseFirestoreException.Code.UNAUTHENTICATED))
        assertEquals(R.string.error_session_ended, resOf(text))
    }

    @Test
    fun `a missing record is named plainly`() {
        val text = ErrorMessages.textFor(firestore(FirebaseFirestoreException.Code.NOT_FOUND))
        assertEquals(R.string.error_record_missing, resOf(text))
    }

    @Test
    fun `an app error carries its own wording through`() {
        val text = ErrorMessages.textFor(AppError(R.string.error_spots_full))
        assertEquals(R.string.error_spots_full, resOf(text))
    }

    @Test
    fun `an app error thrown inside a transaction is still found when wrapped`() {
        // A Firestore transaction hands the failure back wrapped in another exception.
        val wrapped = IllegalStateException("transaction failed", AppError(R.string.error_spots_full))
        assertEquals(R.string.error_spots_full, resOf(ErrorMessages.textFor(wrapped)))
    }

    @Test
    fun `an app error keeps the values that go into its wording`() {
        val text = ErrorMessages.textFor(AppError(R.string.error_generic, listOf(404)))
        assertEquals(listOf(404), (text as UiText.Res).args)
    }

    @Test
    fun `anything unrecognised falls back to the general wording`() {
        assertEquals(ErrorMessages.GENERIC, ErrorMessages.textFor(IOException("something odd")))
        assertEquals(ErrorMessages.GENERIC, ErrorMessages.textFor(RuntimeException()))
    }

    @Test
    fun `every mapping gives a real string resource`() {
        val failures = listOf(
            FirebaseNetworkException("x"),
            firestore(FirebaseFirestoreException.Code.PERMISSION_DENIED),
            firestore(FirebaseFirestoreException.Code.ABORTED),
            IOException("x")
        )
        failures.forEach { error ->
            assertTrue(
                "every failure must map to a resource, not zero",
                resOf(ErrorMessages.textFor(error)) != 0
            )
        }
    }
}
