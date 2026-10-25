package com.seven.mobile.sockets.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.seven.mobile.sockets.R
import com.seven.mobile.sockets.databinding.FragmentRegisterBinding
import com.seven.mobile.sockets.util.ValidationUtils
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilUsername.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

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
                updatePasswordStrength(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.tilConfirmPassword.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val email = binding.etEmail.text?.toString().orEmpty()
            val password = binding.etPassword.text?.toString().orEmpty()
            val confirmPassword = binding.etConfirmPassword.text?.toString().orEmpty()

            var isValid = true
            if (!ValidationUtils.isValidUsername(username)) {
                binding.tilUsername.error = getString(R.string.error_username_invalid)
                isValid = false
            }
            if (!ValidationUtils.isValidEmail(email)) {
                binding.tilEmail.error = getString(R.string.error_invalid_email)
                isValid = false
            }
            if (!ValidationUtils.isValidPassword(password)) {
                binding.tilPassword.error = getString(R.string.error_password_weak)
                isValid = false
            }
            if (password != confirmPassword) {
                binding.tilConfirmPassword.error = getString(R.string.error_passwords_dont_match)
                isValid = false
            }

            if (isValid) {
                viewModel.register(username, email, password, confirmPassword)
            }
        }

        binding.tvGoToLogin.setOnClickListener {
            viewModel.resetState()
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun updatePasswordStrength(password: String) {
        if (password.isEmpty()) {
            binding.llPwdStrength.visibility = View.GONE
            return
        }

        binding.llPwdStrength.visibility = View.VISIBLE
        val strength = ValidationUtils.calculatePasswordStrength(password)
        when (strength) {
            ValidationUtils.PasswordStrength.WEAK -> {
                binding.progressPwdStrength.progress = 33
                binding.progressPwdStrength.setIndicatorColor(requireContext().getColor(R.color.error_red))
                binding.tvPwdStrength.text = getString(R.string.pwd_strength_weak)
            }
            ValidationUtils.PasswordStrength.MEDIUM -> {
                binding.progressPwdStrength.progress = 66
                binding.progressPwdStrength.setIndicatorColor(requireContext().getColor(R.color.orange_warning))
                binding.tvPwdStrength.text = getString(R.string.pwd_strength_medium)
            }
            ValidationUtils.PasswordStrength.STRONG -> {
                binding.progressPwdStrength.progress = 100
                binding.progressPwdStrength.setIndicatorColor(requireContext().getColor(R.color.green_success))
                binding.tvPwdStrength.text = getString(R.string.pwd_strength_strong)
            }
        }
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
        binding.btnRegister.isEnabled = !isLoading
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