package com.example.memoria

import android.content.Context
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import java.time.Instant
import java.time.format.DateTimeFormatter

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
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
        // Inflate the layout for this fragment

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupLoginPrompt(view)
        usernameFocusListener()
        passwordFocusListener()
        registerButtonListener()
    }

    private fun usernameFocusListener() {
        binding.registerUsername.setOnFocusChangeListener { _, focused ->
            if (!focused){
                checkUsername()
            }
        }
    }

    private fun validUsername(): String {
        val username = binding.registerUsername.text.toString()

        if (username.contains("^.*[^a-zA-Z\\d].*$".toRegex())){
            return "Username must only contain alphanumeric characters"
        }

        if (username.isEmpty()) {
            return "Username cannot be blank"
        }

        val user = runBlocking(Dispatchers.IO) {
            dao.findOne(username)
        }


        if(user != null) {
            return "Username already taken"
        }

        return ""
    }

    private fun passwordFocusListener() {
        binding.registerPassword.setOnFocusChangeListener { _, focused ->
            if (!focused){
                checkPassword()
            }
        }
    }

    private fun validPassword(): String {
        val password = binding.registerPassword.text.toString()

        if (password.length < 8){
            return "Password must be at least 8 characters"
        }

        if (!password.contains("[!#$%^&*]?".toRegex())){
            return "Password must contain one special character"
        }

        return ""
    }

    private fun checkUsername() : Boolean {
        val usernameError = validUsername()
        val input = binding.registerUsernameContainer

        if (usernameError != ""){
            input.isErrorEnabled = true
            input.error = usernameError
            return false
        } else {
            input.isErrorEnabled = false
        }

        return true
    }

    private fun checkPassword() : Boolean {
        val passwordError = validPassword()
        val input = binding.registerPasswordContainer

        if (passwordError != ""){
            input.isErrorEnabled = true
            input.error = passwordError
            binding.passwordMin.visibility = View.GONE
            return false
        } else {
            input.isErrorEnabled = false
        }

        return true
    }


    private fun registerButtonListener() {
        binding.registerButton.setOnClickListener() {
            binding.registerUsername.isEnabled = false
            binding.registerPassword.isEnabled = false
            val username = binding.registerUsername.text.toString()
            val password = binding.registerPassword.text.toString()

            val usernameValid = checkUsername()
            val passwordValid = checkPassword()

            if (usernameValid && passwordValid){
                registerUser(username, password)
            } else {
                val toast = Toast.makeText(context, "Please fix errors before continuing!", Toast.LENGTH_LONG)

                toast.show()
            }
            binding.registerUsername.isEnabled = true
            binding.registerPassword.isEnabled = true
        }
    }

    private fun registerUser(username: String, password: String){
        val hashedPass = BCrypt.hashpw(password, BCrypt.gensalt())
        val newUser = User(null, username, hashedPass, DateTimeFormatter.ISO_INSTANT.format(Instant.now()).toString())

        val userId = runBlocking(Dispatchers.IO) {
            dao.insert(newUser)
        }

        val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: return
        with(sharedPref.edit()){
            putString("user_id", userId.toString())
            putString("authenticated_at", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
            apply()
        }

        findNavController().navigate(R.id.action_registerFragment_to_FeedFragment)
    }

    private fun setupLoginPrompt(view: View) {
        val loginPrompt = SpannableString("Already have an account? Login")
        val loginClickable : ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                findNavController().navigate(R.id.action_registerFragment_to_LoginFragment)

            }
        }

        loginPrompt.setSpan(loginClickable, 25, 30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textView = view.findViewById<TextView>(R.id.alreadyHaveAccountPrompt)
        textView.text = loginPrompt
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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}