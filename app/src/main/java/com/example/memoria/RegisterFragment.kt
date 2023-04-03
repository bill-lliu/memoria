package com.example.memoria

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentRegisterBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private var username: EditText? = null
    private var password: EditText? = null
    private var email: EditText? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        username = view.findViewById(R.id.registerUsername)
        password = view.findViewById<EditText>(R.id.registerPassword)

        setupLoginPrompt(view)

        binding.registerButton.setOnClickListener() {
            val toast = Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_LONG)
            val isNotEmpty = checkFieldValues(view)

            if (isNotEmpty) {
                toast.cancel()
                findNavController().navigate(R.id.action_registerFragment_to_FeedFragment)
            } else {
                toast.show()
            }
        }
    }
    private fun checkFieldValues(view: View) : Boolean {
        val usernameText = username?.text.toString()
        val passwordText = password?.text.toString()
        val emailText = email?.text.toString()

        // Check for valid characters in username
        if (usernameText.contains("^.*[^a-zA-Z\\d].*$".toRegex())){
            return false
        }

        // Check for valid characters in password and for valid length
        if (passwordText.length < 8 && !passwordText.contains("!#\$%^&*?".toRegex())){
            return false
        }

        // Check if username and password are non-empty
        if (usernameText.isNotEmpty() && passwordText.isNotEmpty() && emailText.isNotEmpty()){
            return true
        }

        return false
    }

    private fun registerUser(view: View){

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

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}