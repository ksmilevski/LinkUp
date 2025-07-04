package mk.ukim.finki.linkup

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.EventModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchButton: ImageButton
    private lateinit var chatFragment: ChatFragment
    private lateinit var profileFragment: ProfileFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var locationHandler: Handler
    private lateinit var locationRunnable: Runnable
    private var isCheckingNearbyEvents = false

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("declined_invites", MODE_PRIVATE)

        chatFragment = ChatFragment()
        profileFragment = ProfileFragment()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchButton = findViewById(R.id.main_searchButton)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frameLayout, chatFragment)
                        .commit()
                    true
                }
                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frameLayout, profileFragment)
                        .commit()
                    true
                }
                else -> false
            }
        }
        bottomNavigationView.selectedItemId = R.id.menu_chat
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
        forceLocationUpdate()

    }
    private fun forceLocationUpdate() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 seconds for testing
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        android.util.Log.d("LocationUpdate", "Updated Location: (${location.latitude}, ${location.longitude})")
                    }
                }
            }, Looper.getMainLooper())
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (isCheckingNearbyEvents) return
        locationHandler = Handler(mainLooper)
        locationRunnable = Runnable {
            checkNearbyEvents()
            locationHandler.postDelayed(locationRunnable, 10000)
        }
        locationHandler.post(locationRunnable)
        isCheckingNearbyEvents = true
    }

    private fun stopLocationUpdates() {
        if (::locationHandler.isInitialized) {
            locationHandler.removeCallbacks(locationRunnable)
        }
        isCheckingNearbyEvents = false
    }

    private fun checkNearbyEvents() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val userLoc = Location("").apply {
                    latitude = location.latitude
                    longitude = location.longitude
                }

                android.util.Log.d("LocationCheck", "User location - Latitude: ${userLoc.latitude}, Longitude: ${userLoc.longitude}")

                FirebaseFirestore.getInstance().collection("events").get()
                    .addOnSuccessListener { snapshot ->
                        for (doc in snapshot.documents) {
                            val event = doc.toObject(EventModel::class.java) ?: continue
                            val eventLoc = Location("").apply {
                                latitude = event.location.latitude
                                longitude = event.location.longitude
                            }

                            val distance = userLoc.distanceTo(eventLoc)

                            val savedVersion = sharedPreferences.getLong(event.eventId, -1)

                            if (distance <= event.radius) {
                                if (savedVersion >= event.inviteVersion) continue
                                checkIfUserIsInChat(event.chatroomId, event.eventId, inviteVersion = event.inviteVersion)
                            } else {
                                checkIfUserLeftChat(event.chatroomId, event.eventId)
                            }
                        }
                    }
            }
        }
    }


    private fun checkIfUserIsInChat(chatroomId: String, eventId: String, inviteVersion: Long) {
        val userId = FirebaseUtil.currentUserId() ?: return
        FirebaseFirestore.getInstance().collection("chatrooms")
            .document(chatroomId)
            .get()
            .addOnSuccessListener { doc ->
                val chatroom = doc.toObject(ChatRoomModel::class.java)
                if (chatroom != null) {
                    if (!chatroom.userIds.contains(userId)) {
                        showJoinEventPopup(chatroomId, eventId, inviteVersion, chatroom.groupName)
                    }
                }
            }
    }
    private fun checkIfUserLeftChat(chatroomId: String, eventId: String) {
        val userId = FirebaseUtil.currentUserId() ?: return
        FirebaseFirestore.getInstance().collection("chatrooms")
            .document(chatroomId)
            .get()
            .addOnSuccessListener { doc ->
                val chatroom = doc.toObject(ChatRoomModel::class.java)
                if (chatroom != null && chatroom.userIds.contains(userId)) {
                    FirebaseFirestore.getInstance().collection("chatrooms")
                        .document(chatroomId)
                        .update("userIds", FieldValue.arrayRemove(userId))
                }
            }
    }

    private fun showJoinEventPopup(chatroomId: String, eventId: String, inviteVersion: Long, eventName: String) {
        AlertDialog.Builder(this)
            .setTitle("Nearby Event: $eventName")
            .setMessage("You're near an active event! Join the group chat?")
            .setPositiveButton("Join") { _, _ -> joinEventChat(chatroomId) }
            .setNegativeButton("No") { _, _ ->
                sharedPreferences.edit().putLong(eventId, inviteVersion).apply()
            }
            .show()
    }

    private fun joinEventChat(chatroomId: String) {
        val userId = FirebaseUtil.currentUserId() ?: return
        FirebaseFirestore.getInstance().collection("chatrooms")
            .document(chatroomId)
            .update("userIds", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                sharedPreferences.edit().remove(chatroomId).apply()
            }
    }
}





