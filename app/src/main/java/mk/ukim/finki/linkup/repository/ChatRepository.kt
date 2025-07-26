package mk.ukim.finki.linkup.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.android.gms.tasks.await
import mk.ukim.finki.linkup.models.ChatMessageModel
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class ChatRepository {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun getChatRoom(chatroomId: String): ChatRoomModel? {
        val snapshot = firestore.collection("chatrooms").document(chatroomId).get().await()
        return snapshot.toObject(ChatRoomModel::class.java)
    }

    suspend fun createChatRoom(chatRoomModel: ChatRoomModel) {
        firestore.collection("chatrooms").document(chatRoomModel.chatroomId).set(chatRoomModel).await()
    }

    suspend fun getUser(userId: String): UserModel? {
        val snapshot = FirebaseUtil.getUserReference(userId).get().await()
        return snapshot.toObject(UserModel::class.java)
    }

    suspend fun sendMessage(chatroomId: String, message: String, senderId: String) {
        val timestamp = Timestamp.now()
        val chatMessage = ChatMessageModel(message, senderId, timestamp)
        firestore.collection("chatrooms")
            .document(chatroomId)
            .collection("chats")
            .add(chatMessage)
            .await()
        firestore.collection("chatrooms")
            .document(chatroomId)
            .set(
                mapOf(
                    "lastMessage" to message,
                    "lastMessageTimestamp" to timestamp,
                    "lastMessageSenderId" to senderId
                ),
                SetOptions.merge()
            )
            .await()
    }
}
