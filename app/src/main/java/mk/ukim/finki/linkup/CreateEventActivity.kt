package mk.ukim.finki.linkup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.FirebaseFirestore
import mk.ukim.finki.linkup.R
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.EventModel
import mk.ukim.finki.linkup.utils.FirebaseUtil
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createButton = findViewById(R.id.create_event_chat_btn)

        createButton.setOnClickListener {
            requestLocationAndCreateEvent()
        }
    }

    private fun requestLocationAndCreateEvent() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
            return
        }

        val locationRequest = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0) // force fresh location
            .build()

        fusedLocationClient.getCurrentLocation(locationRequest, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    createEventWithLocation(location)
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }

    }

    private fun createEventWithLocation(location: Location) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseUtil.currentUserId() ?: return

        val eventId = UUID.randomUUID().toString()
        val chatroomId = UUID.randomUUID().toString()

        val event = EventModel(
            eventId = eventId,
            location = GeoPoint(location.latitude, location.longitude),
            radius = 100.0,
            chatroomId = chatroomId,
            createdBy = currentUserId,
            timestamp = Timestamp.now()
        )

        val chatroom = ChatRoomModel(
            chatroomId = chatroomId,
            userIds = listOfNotNull(currentUserId),
            isGroup = true,
            groupName = "Event Chat",
            lastMessage = "",
            lastMessageSenderId = "",
            lastMessageTimestamp = Timestamp.now(),
            creatorId = currentUserId // 👈 Save creator here
        )

        db.collection("events").document(eventId).set(event)
        db.collection("chatrooms").document(chatroomId).set(chatroom)
            .addOnSuccessListener {
                Toast.makeText(this, "Event chat created!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create event chat", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndCreateEvent()
        }
    }
}
