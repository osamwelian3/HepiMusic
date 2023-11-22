package com.hepimusic.auth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.amplifyframework.api.ApiCategory
import com.amplifyframework.auth.AuthCategory
import com.amplifyframework.auth.AuthCategoryBehavior
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthPlugin
import com.amplifyframework.auth.AuthProvider
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.category.CategoryType
import com.amplifyframework.datastore.DataStoreCategory
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.category.Category
import com.hepimusic.R
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.ActivityLoginBinding
import com.hepimusic.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(/*R.layout.activity_login*/ binding.root)

        Log.e("AUTH PLUGIN", Amplify.Auth.plugins.joinToString { it.pluginKey })
//        Log.e("AUTH PLUGIN", Amplify.Auth.getPlugin(Amplify.Auth.plugins.joinToString { it.pluginKey }).initialize(this))

        binding.joinLink.setOnClickListener(this)
        binding.usernameField.addTextChangedListener(textWatcher)
        binding.passwordField.addTextChangedListener(textWatcher)
        binding.loginButton.setOnClickListener(this)
        binding.googleLogin.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.joinLink.id -> {
                startActivity(Intent(this, JoinActivity::class.java))
                finish()
            }
            binding.loginButton.id -> login()
            binding.googleLogin.id -> googleLogin()
        }
    }

    private fun googleLogin() {
        Amplify.Auth.signInWithSocialWebUI(
            AuthProvider.google(),
            this@LoginActivity,
            {
                startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                finish()
            },
            {
                runOnUiThread {
                    Log.e("SOCIAL LOGIN ERROR", it.message.toString())
                    Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun login() {
        binding.loginSpinner.crossFadeWidth(binding.loginButton, 500)
        Log.e("USERNAME VM", viewModel.username.value!!)
        Log.e("PASSWORD VM", viewModel.password.value!!)
        Log.e("USERNAME ET", binding.usernameField.text.toString())
        Log.e("PASSWORD ET", binding.passwordField.text.toString())

        Amplify.Auth.signIn(
            viewModel.username.value!!,
            viewModel.password.value!!,
            this::onLoginSuccess,
            this::onLoginError
        )
    }

    private fun onLoginSuccess(authSignInResult: AuthSignInResult) {
        runOnUiThread {
            startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
            finish()
        }
    }

    private fun onLoginError(authException: AuthException) {
        runOnUiThread {
            Toast.makeText(this@LoginActivity, authException.message, Toast.LENGTH_LONG).show()
            Log.e("LOGIN ERROR", authException.message.toString())
            binding.loginButton.crossFadeWidth(binding.loginSpinner, 500)
        }
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
        binding.loginButton.isEnabled = validateData(false)
    }

    private fun validateData(showToast: Boolean): Boolean {
        val username = if (binding.usernameField.text != null) binding.usernameField.text.toString() else ""
        val password = if (binding.passwordField.text != null) binding.passwordField.text.toString() else ""
        viewModel.setUserName(username)
        viewModel.setPassword(password)

        return username != "" && password != ""
    }
}