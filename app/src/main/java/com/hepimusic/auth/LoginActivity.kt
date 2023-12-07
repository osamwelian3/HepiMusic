package com.hepimusic.auth

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
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
import com.amplifyframework.auth.options.AuthWebUISignInOptions
import com.amplifyframework.auth.result.AuthSignInResult
import com.amplifyframework.core.category.CategoryType
import com.amplifyframework.datastore.DataStoreCategory
import com.amplifyframework.core.Amplify
import com.amplifyframework.core.category.Category
import com.hepimusic.R
import com.hepimusic.common.Constants
import com.hepimusic.common.crossFadeWidth
import com.hepimusic.databinding.ActivityLoginBinding
import com.hepimusic.splash.SplashActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var binding: ActivityLoginBinding
    lateinit var viewModel: LoginViewModel
    lateinit var preferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        preferences = this.getSharedPreferences("main", MODE_PRIVATE)
        setContentView(/*R.layout.activity_login*/ binding.root)
        setUp()

        Log.e("AUTH PLUGIN", Amplify.Auth.plugins.joinToString { it.pluginKey })
//        Log.e("AUTH PLUGIN", Amplify.Auth.getPlugin(Amplify.Auth.plugins.joinToString { it.pluginKey }).initialize(this))

        binding.joinLink.setOnClickListener(this)
        binding.usernameField.addTextChangedListener(textWatcher)
        binding.passwordField.addTextChangedListener(textWatcher)
        binding.loginButton.setOnClickListener(this)
        binding.googleLogin.setOnClickListener(this)
        binding.facebookLogin.setOnClickListener(this)
    }

    private fun setUp() {
        val username = preferences.getString(Constants.USERNAME, "") ?: ""
        val userEmail = preferences.getString(Constants.USER_EMAIL, "") ?: ""
        val userPhone = preferences.getString(Constants.USER_PHONE, "") ?: ""
        val userPassword = preferences.getString(Constants.USER_PASSWORD, "") ?: ""

        if (username.isNotEmpty() && userEmail.isNotEmpty() && userPhone.isNotEmpty() && userPassword.isNotEmpty()) {
            startActivity(Intent(this, JoinActivity::class.java))
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            binding.joinLink.id -> {
                startActivity(Intent(this, JoinActivity::class.java))
                finish()
            }
            binding.loginButton.id -> login()
            binding.googleLogin.id -> googleLogin()
            binding.facebookLogin.id -> facebookLogin()
        }
    }

    private fun googleLogin() {
        binding.loginSpinner.crossFadeWidth(binding.loginButton, 500)
        // Link
        /*val url = "https://hsncomj0befx-dev.auth.ap-south-1.amazoncognito.com/oauth2/authorize?identity_provider=Google&redirect_uri=gettingstarted://&response_type=token&client_id=10kmu67jt93r7jukcmsfgd57lq&scope=aws.cognito.signin.user.admin%20email%20openid%20phone%20profile"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)*/
        Amplify.Auth.signInWithSocialWebUI(
            AuthProvider.google(),
            this@LoginActivity,
            {
                preferences.edit().putBoolean(Constants.AUTH_TYPE_SOCIAL, true).apply()
                startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                finish()
            },
            {
                runOnUiThread {
                    Log.e("SOCIAL LOGIN ERROR", it.message.toString())
                    Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
                    binding.loginButton.crossFadeWidth(binding.loginSpinner, 500)
                }
            }
        )
    }

    private fun facebookLogin() {
        binding.loginSpinner.crossFadeWidth(binding.loginButton, 500)
        // Link
        /*val url = "https://hsncomj0befx-dev.auth.ap-south-1.amazoncognito.com/oauth2/authorize?identity_provider=Google&redirect_uri=gettingstarted://&response_type=token&client_id=10kmu67jt93r7jukcmsfgd57lq&scope=aws.cognito.signin.user.admin%20email%20openid%20phone%20profile"
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)*/
        Amplify.Auth.signInWithSocialWebUI(
            AuthProvider.facebook(),
            this@LoginActivity,
            {
                preferences.edit().putBoolean(Constants.AUTH_TYPE_SOCIAL, true).apply()
                startActivity(Intent(this@LoginActivity, SplashActivity::class.java))
                finish()
            },
            {
                runOnUiThread {
                    Log.e("SOCIAL LOGIN ERROR", it.message.toString())
                    Toast.makeText(this@LoginActivity, it.message, Toast.LENGTH_LONG).show()
                    binding.loginButton.crossFadeWidth(binding.loginSpinner, 500)
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
            preferences.edit().putBoolean(Constants.AUTH_TYPE_SOCIAL, false).apply()
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Amplify.Auth.handleWebUISignInResponse(intent)
    }
}