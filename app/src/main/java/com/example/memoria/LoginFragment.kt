package com.example.memoria

import android.content.Context
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private lateinit var dao: UserDao

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance(requireContext()).getUserDao()
        checkIfLoggedIn()

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRegisterPrompt(view)
        onInputFocus()

        binding.loginButton.setOnClickListener() {
            binding.loginUsername.isEnabled = false
            binding.loginPassword.isEnabled = false

            retrieveUser()

            binding.loginUsername.isEnabled = true
            binding.loginPassword.isEnabled = true
        }
    }

    private fun onInputFocus() {
        binding.loginUsername.setOnFocusChangeListener { _, focused ->
            binding.loginUsernameContainer.isErrorEnabled = false
        }

        binding.loginPassword.setOnFocusChangeListener { _, focused ->
            binding.loginPasswordContainer.isErrorEnabled = false
        }
    }

    private fun retrieveUser() {
        val username = binding.loginUsername.text.toString()
        val password = binding.loginPassword.text.toString()
        val userInput = binding.loginUsernameContainer
        val passInput = binding.loginPasswordContainer

        if (username == ""){
            userInput.isErrorEnabled = true
            userInput.error = "Username field cannot be blank"
        }

        if (password == ""){
            passInput.isErrorEnabled = true
            passInput.error = "Password field cannot be blank"
        }

        if (username == "" || password == ""){
            return
        }


        val user = runBlocking(Dispatchers.IO) {
            dao.findOne(username)
        }

        // User not found in database, error out
        if (user == null){
            userInput.isErrorEnabled = true
            passInput.isErrorEnabled = true
            passInput.error = "Incorrect username and/or password"
            return
        } else {
            passInput.isErrorEnabled = false
            userInput.isErrorEnabled = false
        }

        if (checkPassword(user, password)){
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
            with(sharedPref.edit()){
                putString("user_id", user.id.toString())
                putString("authenticated_at", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                apply()
            }

            findNavController().navigate(R.id.action_AuthFragment_to_FeedFragment)
        }
    }

    private fun checkPassword(user: User, password: String) : Boolean {
        val doesMatch = BCrypt.checkpw(password, user.password)
        val input = binding.loginPasswordContainer

        if (!doesMatch){
            input.isErrorEnabled = true
            input.error = "Incorrect username and/or password"
            return false
        } else {
            input.isErrorEnabled = false
        }

        return true
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

    private fun checkIfLoggedIn(): Boolean {
        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return false

        val user = sharedPref.getString("user_id", "")

        if (user != ""){
            findNavController().navigate(R.id.action_global_FeedFragment)
        }

        return false
    }

//    private fun setupForgotPassword(view: View) {
//        val passwordPrompt = SpannableString("Forgot Password?")
//        val passwordClickable : ClickableSpan = object : ClickableSpan() {
//            override fun onClick(p0: View) {
//
//            }
//        }
//
//        passwordPrompt.setSpan(passwordClickable, 0, 16, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
//        val textView = view.findViewById<TextView>(R.id.forgotPassword)
//        textView.text = passwordPrompt
//        textView.movementMethod = LinkMovementMethod.getInstance()
//
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}