package com.example.cropdoctorai.data.repository

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository abstraction layer over Firebase Authentication operations.
 * Handles Google Sign-In credential exchange and Phone OTP verification.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    /** Returns the currently signed-in Firebase user, or null. */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    /** Quick check for existing authenticated session. */
    fun isUserAuthenticated(): Boolean = firebaseAuth.currentUser != null

    /**
     * Signs in with a Google ID token obtained from Credential Manager.
     * Exchanges the ID token for a Firebase Auth credential.
     */
    suspend fun signInWithGoogleCredential(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Authentication failed: user is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Initiates phone number verification via Firebase.
     * Results are delivered through the provided callbacks.
     */
    fun sendPhoneOtp(
        phoneNumber: String,
        activity: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    /**
     * Verifies a phone OTP code against the verification ID received from Firebase.
     */
    suspend fun verifyPhoneOtp(verificationId: String, code: String): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneCredential(credential)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs in with a PhoneAuthCredential (used for both manual OTP and auto-verification).
     */
    suspend fun signInWithPhoneCredential(credential: PhoneAuthCredential): Result<FirebaseUser> {
        return try {
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            authResult.user?.let { Result.success(it) }
                ?: Result.failure(Exception("Phone authentication failed: user is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Signs out the current user from Firebase. */
    fun signOut() {
        firebaseAuth.signOut()
    }
}
