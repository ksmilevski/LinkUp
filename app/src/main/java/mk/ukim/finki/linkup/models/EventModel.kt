package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class EventModel(
    val eventId: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val radius: Double = 100.0, // meters
    val createdBy: String = "",
    val chatroomId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val inviteVersion: Long = 1
)
