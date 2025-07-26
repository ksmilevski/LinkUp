package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatMessageModel (
    var message: String = "",
    var senderId: String = "",
    @get:ServerTimestamp
    var timestamp: Timestamp? = null
)