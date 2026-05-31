package com.example.cropdoctorai.ui.screens.auth

import android.app.Activity
import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cropdoctorai.BuildConfig
import com.example.cropdoctorai.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════
// Auth State Definitions
// ═══════════════════════════════════════════════════════════

sealed class AuthState {
    data object Idle : AuthState()
    data object Loading : AuthState()
    data class OtpSent(val verificationId: String) : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Error(val message: String) : AuthState()
}

// ═══════════════════════════════════════════════════════════
// Auth ViewModel — Google Sign-In + Phone OTP
// ═══════════════════════════════════════════════════════════

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private var verificationId: String? = null

    /** Check if user has an existing authenticated session. */
    fun isUserAuthenticated(): Boolean = authRepository.isUserAuthenticated()

    /** Reset auth state back to idle (e.g., after error or phone number change). */
    fun resetState() {
        _authState.value = AuthState.Idle
        verificationId = null
    }

    // ─── Google Sign-In via Credential Manager ───────────────

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    val authResult = authRepository.signInWithGoogleCredential(idToken)
                    authResult.fold(
                        onSuccess = { user ->
                            _authState.value = AuthState.Authenticated(user)
                        },
                        onFailure = { e ->
                            _authState.value = AuthState.Error(
                                e.message ?: "Google sign-in failed"
                            )
                        }
                    )
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }
            } catch (e: GetCredentialCancellationException) {
                // User cancelled the sign-in flow
                _authState.value = AuthState.Idle
            } catch (e: Exception) {
                _authState.value = AuthState.Error(
                    e.message ?: "Sign-in failed. Please try again."
                )
            }
        }
    }

    // ─── Phone OTP Flow ─────────────────────────────────────

    fun sendOtp(phoneNumber: String, activity: Activity) {
        _authState.value = AuthState.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Auto-verification — sign in directly
                viewModelScope.launch {
                    val result = authRepository.signInWithPhoneCredential(credential)
                    result.fold(
                        onSuccess = { user ->
                            _authState.value = AuthState.Authenticated(user)
                        },
                        onFailure = { e ->
                            _authState.value = AuthState.Error(
                                e.message ?: "Auto-verification failed"
                            )
                        }
                    )
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _authState.value = AuthState.Error(
                    e.message ?: "Phone verification failed"
                )
            }

            override fun onCodeSent(
                id: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                verificationId = id
                _authState.value = AuthState.OtpSent(id)
            }
        }

        authRepository.sendPhoneOtp(phoneNumber, activity, callbacks)
    }

    fun verifyOtp(code: String) {
        val vId = verificationId ?: run {
            _authState.value = AuthState.Error("Verification session expired. Please resend OTP.")
            return
        }

        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.verifyPhoneOtp(vId, code)
            result.fold(
                onSuccess = { user ->
                    _authState.value = AuthState.Authenticated(user)
                },
                onFailure = { e ->
                    _authState.value = AuthState.Error(
                        e.message ?: "OTP verification failed"
                    )
                }
            )
        }
    }
}
