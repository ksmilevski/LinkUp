package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ChatRoomModel(
    var chatroomId: String = "",
    var userIds: List<String> = emptyList(),
    var lastMessageTimestamp: Timestamp = Timestamp(0, 0),
    var lastMessageSenderId: String = "",
    var lastMessage: String = "",

    @get:PropertyName("group")
    @set:PropertyName("group")
    var isGroup: Boolean = false,
    var groupName: String = "",
    var creatorId: String = ""
)
