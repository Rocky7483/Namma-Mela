package com.mindmatrix.nammamela

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mindmatrix.nammamela.databinding.FragmentAuthBinding

class LoginFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAuthBinding.inflate(inflater, container, false).also { b ->
        b.title.text = "Login"
        b.nameInput.visibility = View.GONE
        b.roleGroup.setOnCheckedChangeListener { _, id -> vm.selectedRole.value = if (id == R.id.adminRole) UserRole.ADMIN else UserRole.USER }
        b.primaryButton.text = "Login"
        b.primaryButton.setOnClickListener { vm.login(b.emailInput.text.toString(), b.passwordInput.text.toString()) }
        b.secondaryButton.text = "Create account"
        b.secondaryButton.setOnClickListener { findNavController().navigate(R.id.signupFragment) }
        b.forgotButton.setOnClickListener { findNavController().navigate(R.id.forgotPasswordFragment) }
    }.root
}

class SignupFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAuthBinding.inflate(inflater, container, false).also { b ->
        b.title.text = "Signup"
        b.roleGroup.setOnCheckedChangeListener { _, id -> vm.selectedRole.value = if (id == R.id.adminRole) UserRole.ADMIN else UserRole.USER }
        b.primaryButton.text = "Signup"
        b.primaryButton.setOnClickListener { vm.signup(b.nameInput.text.toString(), b.emailInput.text.toString(), b.passwordInput.text.toString()) }
        b.secondaryButton.text = "Back to login"
        b.secondaryButton.setOnClickListener { findNavController().navigateUp() }
    }.root
}

class ForgotPasswordFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAuthBinding.inflate(inflater, container, false).also { b ->
        b.title.text = "Forgot Password"
        b.nameInput.visibility = View.GONE
        b.passwordInput.visibility = View.GONE
        b.roleGroup.visibility = View.GONE
        b.primaryButton.text = "Send reset link"
        b.primaryButton.setOnClickListener { vm.forgotPassword(b.emailInput.text.toString()) }
        b.secondaryButton.text = "Back to login"
        b.secondaryButton.setOnClickListener { findNavController().navigateUp() }
    }.root
}
