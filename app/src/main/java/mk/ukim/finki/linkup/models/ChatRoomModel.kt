package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp

data class ChatRoomModel(
    var chatroomId: String = "",
    var userIds: List<String> = emptyList(),
    var lastMessageTimestamp: Timestamp = Timestamp.now(),
    var lastMessageSenderId: String = "",
    var lastMessage: String = "",

    var isGroup: Boolean = false,
    var groupName: String = "",
    var creatorId: String = ""
)
