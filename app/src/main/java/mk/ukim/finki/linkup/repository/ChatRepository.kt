package mk.ukim.finki.linkup.repository

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
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
        val chatMessage = ChatMessageModel(message, senderId)
        firestore.collection("chatrooms")
            .document(chatroomId)
            .collection("chats")
            .add(chatMessage)
            .await()
        firestore.collection("chatrooms")
            .document(chatroomId)
            .update(
                mapOf(
                    "lastMessage" to message,
                    "lastMessageTimestamp" to FieldValue.serverTimestamp(),
                    "lastMessageSenderId" to senderId
                )
            )
            .await()
    }

    suspend fun removeUserFromChat(chatroomId: String, userId: String) {
        firestore.collection("chatrooms")
            .document(chatroomId)
            .update("userIds", FieldValue.arrayRemove(userId))
            .await()
    }
}
