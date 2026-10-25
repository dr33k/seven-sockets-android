package com.seven.mobile.sockets.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.material.snackbar.Snackbar
import com.seven.mobile.sockets.R
import com.seven.mobile.sockets.databinding.FragmentLoginBinding
import com.seven.mobile.sockets.util.ValidationUtils
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()
    private lateinit var credentialManager: CredentialManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        credentialManager = CredentialManager.create(requireContext())

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilEmail.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()

            var isValid = true
            if (!ValidationUtils.isValidEmail(email)) {
                binding.tilEmail.error = getString(R.string.error_invalid_email)
                isValid = false
            }
            if (password.isBlank()) {
                binding.tilPassword.error = getString(R.string.error_password_required)
                isValid = false
            }

            if (isValid) {
                viewModel.login(email, password)
            }
        }

        binding.tvForgotPassword.setOnClickListener {
            viewModel.resetState()
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }

        binding.tvGoToRegister.setOnClickListener {
            viewModel.resetState()
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.btnGoogle.setOnClickListener {
            initiateGoogleSignIn()
        }

        binding.btnApple.setOnClickListener {
            initiateAppleSignIn()
        }
    }

    private fun initiateGoogleSignIn() {
        lifecycleScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(getString(R.string.default_web_client_id_placeholder))
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(requireContext(), request)
                val credential = result.credential
                if (credential is GoogleIdTokenCredential) {
                    val idToken = credential.idToken
                    viewModel.handleGoogleOAuth(idToken)
                } else {
                    showError(getString(R.string.error_oauth_failed))
                }
            } catch (e: Exception) {
                showError(e.localizedMessage ?: getString(R.string.error_oauth_failed))
            }
        }
    }

    private fun initiateAppleSignIn() {
        Snackbar.make(binding.root, "Connecting to Apple OAuth endpoint...", Snackbar.LENGTH_SHORT).show()
        val mockAppleToken = "apple_id_token_placeholder"
        viewModel.handleAppleOAuth(mockAppleToken)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { state ->
                    when (state) {
                        is AuthState.Loading -> showLoading(true)
                        is AuthState.Success -> {
                            showLoading(false)
                            (activity as? AuthActivity)?.onAuthSuccess()
                        }
                        is AuthState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                        else -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.btnLogin.isEnabled = !isLoading
        binding.btnGoogle.isEnabled = !isLoading
        binding.btnApple.isEnabled = !isLoading
        binding.progressIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(requireContext().getColor(R.color.error_red))
            .setTextColor(requireContext().getColor(R.color.on_error))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}