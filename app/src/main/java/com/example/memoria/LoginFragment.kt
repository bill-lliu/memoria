package com.example.memoria

import android.content.Intent
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
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentAuthBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class AuthFragment : Fragment() {

    private var _binding: FragmentAuthBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val register_prompt = SpannableString("Don't have an account? Register")
        val registerClickable : ClickableSpan = object : ClickableSpan() {
            override fun onClick(p0: View) {
                val testToast = Toast.makeText(context, "Test button!", Toast.LENGTH_LONG)
                testToast.show()
            }
        }

        register_prompt.setSpan(registerClickable, 23, 31, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        val textView = view.findViewById<TextView>(R.id.register_prompt)
        textView.text = register_prompt
        textView.movementMethod = LinkMovementMethod.getInstance()


        binding.loginButton.setOnClickListener() {
            findNavController().navigate(R.id.action_AuthFragment_to_FeedFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}