package com.example.memoria

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FeedFragment : Fragment() {

    private var _binding: FragmentFeedBinding? = null
    private lateinit var dao: PostDao


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val channelId = "channel1"

    private var allPosts: List<Post> = listOf<Post>()
    private var filteredPosts: List<Post> = listOf<Post>()
    
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

        var savedTime1 = Calendar.getInstance()
        var savedTime2 = Calendar.getInstance()

        binding.changeTimeButton.setOnClickListener {

            // format for hour and minute
            var timeFormat = SimpleDateFormat("hh:mm a", Locale.CANADA)

            // time picker for morning reminder
            val selectedTime1 = savedTime1
            val timePicker1 = TimePickerDialog(
                this.context,
                TimePickerDialog.OnTimeSetListener {
                    view, hourOfDay, minute ->
                        selectedTime1.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        selectedTime1.set(Calendar.MINUTE, minute)
                        Toast.makeText(this.context, "Morning Reminder set to: " + timeFormat.format(selectedTime1.time), Toast.LENGTH_SHORT).show()
                },
                selectedTime1.get(Calendar.HOUR_OF_DAY),
                selectedTime1.get(Calendar.MINUTE),
                false
            )
            savedTime1 = selectedTime1

            // time picker for evening reminder
            val selectedTime2 = savedTime2
            val timePicker2 = TimePickerDialog(
                this.context,
                TimePickerDialog.OnTimeSetListener {
                        view, hourOfDay, minute ->
                    selectedTime2.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    selectedTime2.set(Calendar.MINUTE, minute)
                    Toast.makeText(this.context, "Evening Reminder set to: " + timeFormat.format(selectedTime2.time), Toast.LENGTH_SHORT).show()
                },
                selectedTime2.get(Calendar.HOUR_OF_DAY),
                selectedTime2.get(Calendar.MINUTE),
                false
            )
            savedTime2 = selectedTime2

            // activate pickers
            timePicker2.show()
            timePicker1.show()

            // calculates new delays for reminders
            val currentTime = System.currentTimeMillis()
            val notificationTimeInMillis1 = selectedTime1.timeInMillis
            val notificationTimeInMillis2 = selectedTime2.timeInMillis
            val delayMillis1 = notificationTimeInMillis1 - currentTime
            val delayMillis2 = notificationTimeInMillis2 - currentTime

            val intent = Intent(this.context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this.context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            val builder1 = this.context?.let { it1 ->
                NotificationCompat.Builder(it1, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("It's a new day!")
                    .setContentText("You have a new entry available to make!")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
            }

            val builder2 = this.context?.let { it1 ->
                NotificationCompat.Builder(it1, channelId)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Don't forget to log today!")
                    .setContentText("You haven't made an entry yet! How did you spend your time today?")
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)
            }

            val context1 = this.context
            GlobalScope.launch {
                delay(delayMillis1)
                val notificationManager = context1?.let { it1 -> NotificationManagerCompat.from(it1) }

                if (context1?.let { it1 ->
                        ActivityCompat.checkSelfPermission(
                            it1,
                            Manifest.permission.POST_NOTIFICATIONS
                        )
                    } != PackageManager.PERMISSION_GRANTED
                ) {
                    return@launch
                }
                if (notificationManager != null) {
                    if (builder1 != null) {
                        notificationManager.notify(0, builder1.build())
                    }
                }
            }

            GlobalScope.launch {
                delay(delayMillis2)
                val notificationManager = context1?.let { it1 -> NotificationManagerCompat.from(it1) }
                if (notificationManager != null) {
                    if (builder2 != null) {
                        notificationManager.notify(1, builder2.build())
                    }
                }
            }

        }

        binding.formEntryButton.setOnClickListener {
            findNavController().navigate(R.id.action_FeedFragment_to_FormFragment)
        }

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

        //Observe form to update feed when post is created
        viewModel.postMade.observe(viewLifecycleOwner, Observer { _ ->
            binding.textviewSecond.visibility = View.INVISIBLE
            binding.formEntryButton.isClickable = false
            binding.formEntryButton.alpha = .5f
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadPosts() {
        allPosts = dao.loadPosts().reversed()

        if (allPosts!!.isNotEmpty()) {
            val intro = view?.findViewById(R.id.textview_second) as TextView
            intro.visibility = View.GONE
        }
        filteredPosts = allPosts
        println(filteredPosts)
        val postsView = view?.findViewById(R.id.postList) as RecyclerView
        val adapter = filteredPosts?.let { PostAdapter(it) }
        postsView.adapter = adapter
        postsView.layoutManager = LinearLayoutManager(context)
        postsView.visibility = View.VISIBLE
    }

    private fun updatePosts(query: String?) {
        filteredPosts = getPosts(query)

        val newAdapter = PostAdapter(filteredPosts)
        val postsView = view?.findViewById(R.id.postList) as RecyclerView
        postsView.adapter = newAdapter
        postsView.layoutManager = LinearLayoutManager(context)
        postsView.visibility = View.VISIBLE
    }

    private fun setupSearchView() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                updatePosts(query)
                return true
            }
        })
    }

    private fun getPosts(searchTerm: String?): List<Post> {
        if (allPosts.isEmpty()) {
            return listOf<Post>()
        }

        if (searchTerm == null) {
            return allPosts
        }

        val cleanedTerm = searchTerm.trim().lowercase()

        return allPosts!!.filter {
                post -> post.title.lowercase().contains(cleanedTerm) or
                post.description.lowercase().contains(cleanedTerm) or
                post.tags.contains(cleanedTerm)
        }
    }
}