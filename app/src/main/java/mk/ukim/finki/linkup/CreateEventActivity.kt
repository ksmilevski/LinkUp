package mk.ukim.finki.linkup

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
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
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.EventModel
import mk.ukim.finki.linkup.utils.FirebaseUtil
import java.util.*

class CreateEventActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var eventNameInput: EditText
    private lateinit var radiusInput: EditText
    private lateinit var createEventButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        eventNameInput = findViewById(R.id.event_name_input)
        radiusInput = findViewById(R.id.event_radius_input)
        createEventButton = findViewById(R.id.create_event_button)

        createEventButton.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    1001
                )
                return@setOnClickListener
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    createEventWithLocation(location)
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.toast_unable_get_location),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
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
                    Toast.makeText(
                        this,
                        getString(R.string.toast_unable_fetch_location),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

    }

    private fun createEventWithLocation(location: Location) {
        val db = FirebaseFirestore.getInstance()
        val currentUserId = FirebaseUtil.currentUserId() ?: return

        val eventName = eventNameInput.text.toString().trim()
        val radius = radiusInput.text.toString().toDoubleOrNull()

        if (eventName.isEmpty() || radius == null) {
            Toast.makeText(
                this,
                getString(R.string.toast_enter_event_details),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val eventRef = db.collection("events").document()
        val eventId = eventRef.id
        val chatroomRef = db.collection("chatrooms").document()
        val chatroomId = chatroomRef.id

        val event = EventModel(
            eventId = eventId,
            location = GeoPoint(location.latitude, location.longitude),
            radius = radius,
            chatroomId = chatroomId,
            createdBy = currentUserId,
            timestamp = Timestamp.now(),
            eventName = eventName
        )

        val chatroom = ChatRoomModel(
            chatroomId = chatroomId,
            userIds = listOfNotNull(currentUserId),
            isGroup = true,
            groupName = eventName,
            lastMessage = "",
            lastMessageSenderId = "",
            creatorId = currentUserId
        )

        eventRef.set(event)
        chatroomRef.set(chatroom)
            .addOnSuccessListener {
                Toast.makeText(
                    this,
                    getString(R.string.toast_event_created),
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.toast_event_create_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            requestLocationAndCreateEvent()
        }
    }
}
