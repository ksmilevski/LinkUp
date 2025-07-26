package mk.ukim.finki.linkup.utils

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale

class FirebaseUtil {

    companion object {
        fun currentUserId(): String? {
            return FirebaseAuth.getInstance().uid
        }

        fun isLoggedIn(): Boolean {
            return currentUserId() != null
        }

        fun currentUserDetails(): DocumentReference? {
            val userId = currentUserId()
            return userId?.let {
                //ke go dobieme user uid i ke go napravime key po koj ke gi store users vo kolekcija
                FirebaseFirestore.getInstance().collection("users").document(it)
            }
        }

        //vrati gi site users od kolekcijata users
        fun allUserCollectionReference(): CollectionReference {
            return FirebaseFirestore.getInstance().collection("users")
        }

        fun getChatroomReference(chatroomId: String): DocumentReference {
            return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId)
        }

        //vo kolekcijata chatroom ke ima kolekcija chats vo koja ke gi ima porakite
        fun getChatroomMessageReference(chatroomId: String): CollectionReference {
            return getChatroomReference(chatroomId).collection("chats")
        }

        //ako user1 chatuva vo user2 i user2 so user1, da vrakja ist chatroomId
        fun getChatroomId(userId1: String, userId2: String): String {
            return if (userId1.hashCode() < userId2.hashCode()) {
                userId1+"_"+userId2
            } else {
                userId2+"_"+userId1
            }
        }

        //gi vrakja site od chatroom collection
        fun allChatroomCollectionReference(): CollectionReference {
            return FirebaseFirestore.getInstance().collection("chatrooms")
        }

        fun getOtherUserFromChatroom(userIds: List<String>): DocumentReference {
            val currentUserId = FirebaseUtil.currentUserId()
            val otherUserId = if (userIds[0] == currentUserId) userIds[1] else userIds[0]
            return allUserCollectionReference().document(otherUserId)
        }

        fun timestampToString(timestamp: Timestamp?): String {
            if (timestamp == null) return ""
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(timestamp.toDate())
        }

        fun logout(){
            FirebaseAuth.getInstance().signOut()
        }
        fun getUserReference(userId: String): DocumentReference {
            require(userId.isNotEmpty()) { "Invalid userId: cannot be empty" }
            return FirebaseFirestore.getInstance().collection("users").document(userId)
        }

    }
}