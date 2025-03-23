package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp

data class ChatMessageModel (
    var message:String = "",
    var senderId:String = "",
    var timestamp:Timestamp = Timestamp.now()
)