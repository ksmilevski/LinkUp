package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class ChatRoomModel(
    var chatroomId: String = "",
    var userIds: List<String> = emptyList(),
    @get:ServerTimestamp
    var lastMessageTimestamp: Timestamp? = null,
    var lastMessageSenderId: String = "",
    var lastMessage: String = "",

    @get:PropertyName("group")
    @set:PropertyName("group")
    var isGroup: Boolean = false,
    var groupName: String = "",
    var creatorId: String = ""
)
