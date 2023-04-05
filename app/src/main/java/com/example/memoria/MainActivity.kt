package com.example.memoria

import android.Manifest
//import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.icu.util.Calendar
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.memoria.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val channelId = "channel1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        setSupportActionBar(binding.toolbar)

        createNotificationChannel()

        val notificationTime1 = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 36)
            set(Calendar.SECOND, 0)
        }

        val notificationTime2 = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 3)
            set(Calendar.MINUTE, 22)
            set(Calendar.SECOND, 0)
        }

        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder1 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("It's a new day!")
            .setContentText("You have a new entry available to make!")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val builder2 = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle("Don't forget to log today!")
            .setContentText("You haven't made an entry yet! How did you spend your time today?")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val currentTime = System.currentTimeMillis()
        val notificationTimeInMillis1 = notificationTime1.timeInMillis
        val notificationTimeInMillis2 = notificationTime2.timeInMillis
        val delayMillis1 = notificationTimeInMillis1 - currentTime
        val delayMillis2 = notificationTimeInMillis2 - currentTime

        val context1 = this
        GlobalScope.launch {
            delay(delayMillis1)
            val notificationManager = NotificationManagerCompat.from(context1)

            if (ActivityCompat.checkSelfPermission(
                    context1,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@launch
            }
            notificationManager.notify(0, builder1.build())
        }

        GlobalScope.launch {
            delay(delayMillis2)
            val notificationManager = NotificationManagerCompat.from(context1)
            notificationManager.notify(1, builder2.build())
        }

        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        appBarConfiguration = AppBarConfiguration(navController.graph)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        checkIfLoggedIn()
    }

    private fun checkIfLoggedIn() {
        val sharedPref = this.getPreferences(Context.MODE_PRIVATE) ?: return

        val user = sharedPref.getString("user_id", "")

        if (user != ""){
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.action_global_FeedFragment)
        }

    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navController = findNavController(R.id.nav_host_fragment_content_main)
//        return navController.navigateUp(navController.graph)
//                || super.onSupportNavigateUp()
////        return super.onSupportNavigateUp()
//    }
    private fun createNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descText
            }
            val notificationManager: NotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}