package com.example.easychat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import mk.ukim.finki.linkup.R
import mk.ukim.finki.linkup.models.ChatMessageModel
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class ChatRecyclerAdapter(
    options: FirestoreRecyclerOptions<ChatMessageModel>,
    private val context: Context,
    private val chatroomModel: ChatRoomModel
) : FirestoreRecyclerAdapter<ChatMessageModel, ChatRecyclerAdapter.ChatMessageViewHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.chat_message_recycler_row, parent, false)
        return ChatMessageViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ChatMessageViewHolder,
        position: Int,
        model: ChatMessageModel
    ) {
        val isSentByCurrentUser = model.senderId == FirebaseUtil.currentUserId()

        if (isSentByCurrentUser) {
            holder.rightChatLayout.visibility = View.VISIBLE
            holder.leftChatLayout.visibility = View.GONE
            holder.rightChatText.text = model.message

            if (chatroomModel.isGroup) {
                holder.senderName.visibility = View.GONE
                holder.senderNameRight.visibility = View.VISIBLE
                holder.senderNameRight.text = "Me"
            } else {
                holder.senderName.visibility = View.GONE
                holder.senderNameRight.visibility = View.GONE
            }

        } else {
            holder.leftChatLayout.visibility = View.VISIBLE
            holder.rightChatLayout.visibility = View.GONE
            holder.leftChatText.text = model.message
            holder.senderNameRight.visibility = View.GONE

            if (chatroomModel.isGroup) {
                holder.senderName.visibility = View.VISIBLE
                FirebaseUtil.getUserReference(model.senderId)
                    .get()
                    .addOnSuccessListener {
                        val senderUser = it.toObject(mk.ukim.finki.linkup.models.UserModel::class.java)
                        holder.senderName.text = senderUser?.username ?: "Unknown"
                    }
            } else {
                holder.senderName.visibility = View.GONE
            }
        }

    }

    class ChatMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val leftChatText: TextView = itemView.findViewById(R.id.left_chat_textview)
        val rightChatText: TextView = itemView.findViewById(R.id.right_chat_textview)
        val leftChatLayout: LinearLayout = itemView.findViewById(R.id.left_chat_layout)
        val rightChatLayout: LinearLayout = itemView.findViewById(R.id.right_chat_layout)
        val senderName: TextView = itemView.findViewById(R.id.sender_name_textview)
        val senderNameRight: TextView = itemView.findViewById(R.id.sender_name_right)

    }
}
