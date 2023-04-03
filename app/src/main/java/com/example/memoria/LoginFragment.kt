package com.example.memoria

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentLoginBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private var username: EditText? = null
    private var password: EditText? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        username = view.findViewById(R.id.loginUsername)
        password = view.findViewById<EditText>(R.id.loginPassword)

        setupRegisterPrompt(view)
        setupForgotPassword(view)

        binding.loginButton.setOnClickListener() {
            val toast = Toast.makeText(context, "Please enter username and/or password!", Toast.LENGTH_LONG)
            val isNotEmpty = checkFieldValues(view)

            if (isNotEmpty) {
                toast.cancel()
                findNavController().navigate(R.id.action_AuthFragment_to_FeedFragment)
            } else {
                toast.show()
            }
        }
    }

    private fun checkFieldValues(view: View) : Boolean {
        val usernameText = username?.text.toString()
        val passwordText = password?.text.toString()

        // Check for valid characters in username
        if (usernameText.contains("^.*[^a-zA-Z\\d].*$".toRegex())){
            return false
        }



        // Check if username and password are non-empty
        if (usernameText.isNotEmpty() && passwordText.isNotEmpty()){
            return true
        }

        return false
    }

    private fun setupRegisterPrompt(view: View) {
        val registerPrompt = SpannableString("Don't have an account? Register")
        val registerClickable : ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                findNavController().navigate(R.id.action_LoginFragment_to_registerFragment)

            }
        }

        registerPrompt.setSpan(registerClickable, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textView = view.findViewById<TextView>(R.id.registerPrompt)
        textView.text = registerPrompt
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun setupForgotPassword(view: View) {
        val passwordPrompt = SpannableString("Forgot Password?")
        val passwordClickable : ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {

            }
        }

        passwordPrompt.setSpan(passwordClickable, 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val textView = view.findViewById<TextView>(R.id.forgotPassword)
        textView.text = passwordPrompt
        textView.movementMethod = LinkMovementMethod.getInstance()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}