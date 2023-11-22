package com.hepimusic.auth

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.AuthUserAttribute
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.auth.result.AuthSignUpResult
import com.amplifyframework.auth.result.step.AuthSignUpStep
import com.amplifyframework.core.Amplify
import com.hepimusic.common.Constants.USERNAME
import com.hepimusic.common.Constants.USER_EMAIL
import com.hepimusic.common.Constants.USER_PASSWORD
import com.hepimusic.common.Constants.USER_PHONE
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.ActivityJoinBinding
import com.hepimusic.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class JoinActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityJoinBinding
    lateinit var viewModel: JoinViewModel
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = this.getSharedPreferences("main", MODE_PRIVATE)
        viewModel = ViewModelProvider(this)[JoinViewModel::class.java]
        binding = ActivityJoinBinding.inflate(LayoutInflater.from(this))
        setContentView(/*R.layout.activity_join*/ binding.root)
        setUp()

        binding.loginLink.setOnClickListener(this)
        binding.joinButton.setOnClickListener(this)
        binding.verifyButton.setOnClickListener(this)
        binding.resendVerificationLink.setOnClickListener(this)
        binding.backToSignUp.setOnClickListener(this)
        binding.googleLogin.setOnClickListener(this)
        binding.usernameField.addTextChangedListener(textWatcher)
        binding.passwordField.addTextChangedListener(textWatcher)
        binding.emailField.addTextChangedListener(textWatcher)
        binding.phoneField.addTextChangedListener(textWatcher)
        binding.verificationCodeField.addTextChangedListener(vTextWatcher)
    }

    private fun setUp() {
        val username = preferences.getString(USERNAME, "") ?: ""
        val userEmail = preferences.getString(USER_EMAIL, "") ?: ""
        val userPhone = preferences.getString(USER_PHONE, "") ?: ""
        val userPassword = preferences.getString(USER_PASSWORD, "") ?: ""

        if (username.isNotEmpty() && userEmail.isNotEmpty() && userPhone.isNotEmpty() && userPassword.isNotEmpty()) {
            viewModel.setUserName(username)
            viewModel.setEmail(userEmail)
            viewModel.setPhone(userPhone)
            viewModel.setPassword(userPassword)

            binding.verificationForm.crossFadeWidth(binding.signUpForm, 100)
        }
    }

    private fun clearPrefs() {
        val username = preferences.getString(USERNAME, "") ?: ""
        val userEmail = preferences.getString(USER_EMAIL, "") ?: ""
        val userPhone = preferences.getString(USER_PHONE, "") ?: ""
        val userPassword = preferences.getString(USER_PASSWORD, "") ?: ""
        if (username.isNotEmpty() && userEmail.isNotEmpty() && userPhone.isNotEmpty() && userPassword.isNotEmpty()) {
            Toast.makeText(
                this,
                "Unconfirmed user account with username: $username and email: $userEmail will be deleted if not verified within seven days. Check your email to verify.",
                Toast.LENGTH_LONG
            ).show()
        }
        preferences.edit().remove(USERNAME).apply()
        preferences.edit().remove(USER_EMAIL).apply()
        preferences.edit().remove(USER_PHONE).apply()
        preferences.edit().remove(USER_PASSWORD).apply()

        binding.signUpForm.crossFadeWidth(binding.verificationForm, 500)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.loginLink.id -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            binding.joinButton.id -> join()
            binding.resendVerificationLink.id -> {
                Log.e("RESEND CODE", viewModel.username.value!!.toString())
                binding.resendVerificationSpinner.crossFadeWidth(binding.resendVerificationLink, 500, 0, View.INVISIBLE)
                Amplify.Auth.resendSignUpCode(
                    viewModel.username.value!!,
                    {
                        runOnUiThread {
                            binding.resendVerificationLink.crossFadeWidth(binding.resendVerificationSpinner, 500, 0, View.GONE)
                            Toast.makeText(this@JoinActivity, "Code resent successfully", Toast.LENGTH_LONG).show()
                        }
                    },
                    {
                        runOnUiThread {
                            binding.resendVerificationLink.crossFadeWidth(binding.resendVerificationSpinner, 500, 0, View.GONE)
                            Toast.makeText(this@JoinActivity, it.message.toString(), Toast.LENGTH_LONG).show()
                        }
                    }
                )
            }
            binding.backToSignUp.id -> clearPrefs()
            binding.verifyButton.id -> verifyAccount()
            binding.googleLogin.id -> googleLogin()
        }
    }

    private fun googleLogin() {
        Amplify.Auth.signInWithSocialWebUI(
            AuthProvider.google(),
            this@JoinActivity,
            {
                startActivity(Intent(this@JoinActivity, SplashActivity::class.java))
                finish()
            },
            {
                Toast.makeText(this@JoinActivity, it.message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun join() {
        Log.e("USERNAME VM", viewModel.username.value!!)
        Log.e("PASSWORD VM", viewModel.password.value!!)
        Log.e("PHONE VM", viewModel.phone.value!!)
        Log.e("EMAIL VM", viewModel.emailAddr.value!!)
        binding.registerSpinner.crossFadeWidth(binding.joinButton, 500)
        val attributes = mutableListOf<AuthUserAttribute>()
        val emailAttribute = AuthUserAttribute(AuthUserAttributeKey.email(), binding.emailField.text.toString())
        val phoneAttribute = AuthUserAttribute(AuthUserAttributeKey.phoneNumber(), binding.phoneField.text.toString())
        attributes.add(emailAttribute)
        attributes.add(phoneAttribute)
        val options: AuthSignUpOptions = AuthSignUpOptions.builder()
            .userAttributes(attributes)
            .build()
        Amplify.Auth.signUp(
            viewModel.username.value!!,
            viewModel.password.value!!,
            options,
            this::onSignUpSuccess,
            this::onSignUpError
        )
    }

    private fun onSignUpSuccess(authSignUpResult: AuthSignUpResult) {
        runOnUiThread {
            preferences.edit().putString(USERNAME, binding.usernameField.text.toString()).apply()
            preferences.edit().putString(USER_EMAIL, binding.emailField.text.toString()).apply()
            preferences.edit().putString(USER_PHONE, binding.phoneField.text.toString()).apply()
            preferences.edit().putString(USER_PASSWORD, binding.passwordField.text.toString()).apply()
            /*startActivity(Intent(this@JoinActivity, LoginActivity::class.java))
            finish()*/
            binding.verificationForm.crossFadeWidth(binding.signUpForm, 500)
        }
    }

    private fun onSignUpError(authException: AuthException) {
        runOnUiThread {
            Toast.makeText(this@JoinActivity, authException.message, Toast.LENGTH_LONG).show()
            Log.e("JOIN ERROR", authException.message.toString())
            binding.joinButton.crossFadeWidth(binding.registerSpinner, 500)
        }
        binding.joinButton.crossFadeWidth(binding.registerSpinner, 500)
    }

    private fun verifyAccount() {
        Amplify.Auth.confirmSignUp(
            viewModel.username.value!!,
            binding.verificationCodeField.text.toString(),
            {
                if (it.isSignUpComplete) {
                    login()
                }
            },
            {
                Toast.makeText(this@JoinActivity, it.message, Toast.LENGTH_LONG).show()
            }
        )
    }

    private fun login() {
        Amplify.Auth.signIn(
            viewModel.username.value!!,
            viewModel.password.value!!,
            {
                startActivity(Intent(this@JoinActivity, SplashActivity::class.java))
                finish()
            },
            {
                Toast.makeText(this@JoinActivity, it.message, Toast.LENGTH_LONG).show()
            }
        )
    }

    val vTextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableVerifyButton()
        }

        override fun afterTextChanged(s: Editable?) {
            enableVerifyButton()
        }
    }

    private fun enableVerifyButton() {
        binding.verifyButton.isEnabled = binding.verificationCodeField.text.toString().isNotEmpty()
    }

    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            enableLoginButton()
        }

        override fun afterTextChanged(s: Editable?) {
            enableLoginButton()
        }
    }

    private fun enableLoginButton() {
        binding.joinButton.isEnabled = validateData(false)
    }

    private fun validateData(showToast: Boolean): Boolean {
        val username = if (binding.usernameField.text != null) binding.usernameField.text.toString() else ""
        val password = if (binding.passwordField.text != null) binding.passwordField.text.toString() else ""
        val emailAddr = if (binding.emailField.text != null) binding.emailField.text.toString() else ""
        val phone = if (binding.phoneField.text != null) binding.phoneField.text.toString() else ""
        viewModel.setUserName(username)
        viewModel.setPassword(password)
        viewModel.setEmail(emailAddr)
        viewModel.setPhone(phone)

        if (showToast && !(username != "" && password != "" && emailAddr != "" && phone != "")) {
            runOnUiThread {
                Toast.makeText(this@JoinActivity, "All Fields are required. Please fill in all the fields to proceed", Toast.LENGTH_LONG).show()
            }
        }
        return username != "" && password != "" && emailAddr != "" && phone != ""
    }
}