package mk.ukim.finki.linkup.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import mk.ukim.finki.linkup.ChatActivity
import mk.ukim.finki.linkup.R
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.AndroidUtil
import mk.ukim.finki.linkup.utils.FirebaseUtil

class RecentChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatRoomModel>,
    private val context: Context
) : FirestoreRecyclerAdapter<ChatRoomModel, RecentChatRecyclerAdapter.ChatroomModelViewHolder>(options) {

    override fun onBindViewHolder(holder: ChatroomModelViewHolder, position: Int, model: ChatRoomModel) {
        FirebaseUtil.getOtherUserFromChatroom(model.userIds)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val otherUserModel = task.result?.toObject(UserModel::class.java)
                    val lastMessageSentByMe = model.lastMessageSenderId == FirebaseUtil.currentUserId()

                    otherUserModel?.let { user ->
//                        FirebaseUtil.getOtherProfilePicStorageRef(user.userId).downloadUrl
//                            .addOnCompleteListener { t ->
//                                if (t.isSuccessful) {
//                                    val uri: Uri? = t.result
//                                    uri?.let { AndroidUtil.setProfilePic(context, it, holder.profilePic) }
//                                }
//                            }

                        holder.usernameText.text = user.username
                        holder.lastMessageText.text = if (lastMessageSentByMe) "You: ${model.lastMessage}" else model.lastMessage
                        holder.lastMessageTime.text = FirebaseUtil.timestampToString(model.lastMessageTimestamp)

                        holder.itemView.setOnClickListener {
                            val intent = Intent(context, ChatActivity::class.java).apply {
                                AndroidUtil.passUserModelAsIntent(this, user)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatroomModelViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_chat_recycler_row, parent, false)
        return ChatroomModelViewHolder(view)
    }

    class ChatroomModelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        val lastMessageText: TextView = itemView.findViewById(R.id.last_message_text)
        val lastMessageTime: TextView = itemView.findViewById(R.id.last_message_time_text)
        val profilePic: ImageView = itemView.findViewById(R.id.profile_pic_image_view)
    }
}
