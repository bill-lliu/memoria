package com.example.memoria

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.findNavController
import com.example.memoria.databinding.FragmentFeedBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var allPosts: List<Post>? = null
    private var filteredPosts: List<Post>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        loadPosts()

        setupSearchView()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.logOutButton.setOnClickListener {
            findNavController().navigate(R.id.action_FeedFragment_to_AuthFragment)
        }
        binding.formEntryButton.setOnClickListener {
            findNavController().navigate(R.id.action_FeedFragment_to_FormFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPosts() {
        // TODO: Load posts here

        allPosts = listOf<Post>()
        filteredPosts = allPosts
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filteredPosts = getPosts(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun getPosts(tag: String?): List<Post> {
        if (tag == null || allPosts == null) {
            return allPosts ?: listOf<Post>()
        }

        val cleanedTag = tag.trim().lowercase()

        return allPosts!!.filter { post -> post.tags.contains(cleanedTag) }
    }

}