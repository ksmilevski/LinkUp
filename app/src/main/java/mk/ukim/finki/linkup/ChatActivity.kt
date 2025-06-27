package mk.ukim.finki.linkup

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.easychat.adapter.ChatRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.AndroidUtil
import mk.ukim.finki.linkup.utils.FirebaseUtil
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import mk.ukim.finki.linkup.models.ChatMessageModel
import mk.ukim.finki.linkup.adapter.GroupMemberAdapter
import android.view.Gravity

class ChatActivity : AppCompatActivity() {

    private lateinit var otherUser: UserModel
    private lateinit var chatroomModel: ChatRoomModel
    private lateinit var messageInput: EditText
    private lateinit var sendMessageBtn: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var otherUsername: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatroomId: String
    private lateinit var adapter: ChatRecyclerAdapter
    private lateinit var resendInviteButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: GroupMemberAdapter
    private lateinit var membersBtn: ImageButton
    private val memberList = mutableListOf<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        resendInviteButton = findViewById(R.id.resend_invite_button)
        resendInviteButton.setOnClickListener {
            resendInvitations()
        }

        chatroomId = intent.getStringExtra("chatroomId") ?: run {
            val otherUserModel = AndroidUtil.getUserModelFromIntent(intent)
            chatroomId = FirebaseUtil.getChatroomId(
                FirebaseUtil.currentUserId() ?: throw IllegalStateException("User ID is null"),
                otherUserModel.userId
            )
            otherUser = otherUserModel
            chatroomId
        }

        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backButton = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        membersRecyclerView = findViewById(R.id.members_recycler_view)
        membersBtn = findViewById(R.id.members_btn)

        membersRecyclerView.layoutManager = LinearLayoutManager(this)
        membersAdapter = GroupMemberAdapter(memberList)
        membersRecyclerView.adapter = membersAdapter
        membersBtn.setOnClickListener { drawerLayout.openDrawer(Gravity.END) }
        membersBtn.visibility = View.GONE

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        sendMessageBtn.setOnClickListener {
            val message = messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessageToUser(message)
            }
        }

        getOrCreateChatroomModel()
    }


    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java)
            .build()

        adapter = ChatRecyclerAdapter(options, applicationContext, chatroomModel)

        val manager = LinearLayoutManager(this).apply {
            reverseLayout = true // gi setira porakite vo descending order, poslednoto pishano e najdolu
        }

        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter
        adapter.startListening()

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        })
    }


    private fun sendMessageToUser(message:String){
        chatroomModel.lastMessageTimestamp = Timestamp.now()
        chatroomModel.lastMessageSenderId = FirebaseUtil.currentUserId()?:"unknown user"
        chatroomModel.lastMessage = message
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel)

        val chatMessageModel = ChatMessageModel(message, FirebaseUtil.currentUserId()?:"unknown user", Timestamp.now())
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel).addOnCompleteListener{
            task-> if(task.isSuccessful){
                messageInput.setText("") //ako se prati porakata, sledno treba da e prazno toa kaj shto se pishuva
//                sendNotification(message)
            }
        }
    }


    //proveruva dali postoi veke chatroom ili da kreira nova
//    private fun getOrCreateChatroomModel() {
//        FirebaseUtil.getChatroomReference(chatroomId).get().addOnSuccessListener { document ->
//            if (document.exists()) {
//                chatroomModel = document.toObject(ChatRoomModel::class.java)!!
//
//                if (chatroomModel.isGroup) {
//                    otherUsername.text = chatroomModel.groupName
//                } else {
//                    val otherUserId = chatroomModel.userIds.first { it != FirebaseUtil.currentUserId() }
//                    FirebaseUtil.getUserReference(otherUserId).get().addOnSuccessListener { userDoc ->
//                        val otherUser = userDoc.toObject(UserModel::class.java)
//                        otherUsername.text = otherUser?.username ?: "User"
//                    }
//                }
//
//                setupChatRecyclerView() // ✅ only call after setting up model and UI
//            } else {
//                Toast.makeText(this, "Chatroom not found!", Toast.LENGTH_SHORT).show()
//                finish()
//            }
//        }
//    }

    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnSuccessListener { document ->
            if (document.exists()) {
                chatroomModel = document.toObject(ChatRoomModel::class.java)!!

                if (chatroomModel.isGroup) {
                    otherUsername.text = chatroomModel.groupName
                    membersBtn.visibility = View.VISIBLE
                    if (chatroomModel.creatorId == FirebaseUtil.currentUserId()) {
                        resendInviteButton.visibility = View.VISIBLE
                    } else {
                        resendInviteButton.visibility = View.GONE
                    }
                    loadGroupMembers()
                } else {
                    val otherUserId = chatroomModel.userIds.first { it != FirebaseUtil.currentUserId() }
                    FirebaseUtil.getUserReference(otherUserId).get().addOnSuccessListener { userDoc ->
                        val otherUser = userDoc.toObject(UserModel::class.java)
                        otherUsername.text = otherUser?.username ?: "User"
                    }
                    membersBtn.visibility = View.GONE
                }

                setupChatRecyclerView()
            } else {
                Toast.makeText(this, "Chatroom not found!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
    private fun resendInvitations() {
        FirebaseFirestore.getInstance().collection("events")
            .whereEqualTo("chatroomId", chatroomId)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val eventDoc = snapshot.documents[0]
                    eventDoc.reference.update("inviteVersion", FieldValue.increment(1))
                        .addOnSuccessListener {
                            Toast.makeText(this, "Invitations resent!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to resend invitations!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }
    private fun loadGroupMembers() {
        memberList.clear()
        for (id in chatroomModel.userIds) {
            FirebaseUtil.getUserReference(id).get().addOnSuccessListener { doc ->
                doc.toObject(UserModel::class.java)?.let { user ->
                    memberList.add(user)
                    membersAdapter.notifyDataSetChanged()
                }
            }
        }
    }

}
