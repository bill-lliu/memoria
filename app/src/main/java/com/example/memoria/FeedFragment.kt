package com.example.memoria

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.memoria.databinding.FragmentFeedBinding
import com.example.memoria.FormViewModel


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private lateinit var dao: PostDao


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var allPosts: List<Post>? = null
    private var filteredPosts: List<Post>? = null
    
    private val viewModel: FormViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dao = AppDatabase.getInstance(requireContext()).getPostDao()
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFeedBinding.inflate(inflater, container, false)

        setupSearchView()

        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadPosts()

        binding.logOutButton.setOnClickListener {
            findNavController().navigate(R.id.action_FeedFragment_to_AuthFragment)
            val sharedPref = activity?.getPreferences(Context.MODE_PRIVATE) ?: null

            if (sharedPref != null){
                with(sharedPref.edit()) {
                    remove("user_id").commit()
                    remove("authorized_at").commit()
                }
            }

        }
        binding.formEntryButton.setOnClickListener {
            findNavController().navigate(R.id.action_FeedFragment_to_FormFragment)
        }

        //Observe form to update feed when post is created
        viewModel.postMade.observe(viewLifecycleOwner, Observer { _ ->
            binding.textviewSecond.visibility = View.INVISIBLE
            binding.formEntryButton.isClickable = false
            binding.formEntryButton.alpha = .5f
            val toast = Toast.makeText(context, "Post created!", Toast.LENGTH_LONG)
            toast.show()
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPosts() {
        // TODO: Load posts here

        allPosts = dao.loadPosts().reversed()
        if (allPosts!!.isNotEmpty()) {
            val intro = view?.findViewById(R.id.textview_second) as TextView
            intro.visibility = View.GONE
        }
        val filteredPosts = allPosts
        println(filteredPosts)
        val postsView = view?.findViewById(R.id.postList) as RecyclerView
        val adapter = filteredPosts?.let { PostAdapter(it) }
        postsView.adapter = adapter
        postsView.layoutManager = LinearLayoutManager(context)
        postsView.visibility = View.VISIBLE
    }

    private fun setupSearchView() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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

        return allPosts!!.filter {
                post -> post.title.lowercase().contains(cleanedTag) or
                post.description.lowercase().contains(cleanedTag) or
                post.tags.contains(cleanedTag)
        }
    }
}