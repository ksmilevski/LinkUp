package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
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
import mk.ukim.finki.linkup.databinding.ActivityChatBinding
import mk.ukim.finki.linkup.repository.ChatRepository
import mk.ukim.finki.linkup.repository.ChatViewModel
import mk.ukim.finki.linkup.repository.ChatViewModelFactory

class ChatActivity : AppCompatActivity() {

    private lateinit var otherUser: UserModel
    private lateinit var chatroomModel: ChatRoomModel

    private lateinit var binding: ActivityChatBinding

    private lateinit var chatroomId: String
    private lateinit var adapter: ChatRecyclerAdapter

    private lateinit var recyclerView: RecyclerView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var membersRecyclerView: RecyclerView
    private lateinit var membersAdapter: GroupMemberAdapter
    private lateinit var leaveGroupButton: Button

    private val memberList = mutableListOf<UserModel>()

    private val repository = ChatRepository()

    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.resendInviteButton.setOnClickListener {
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

        drawerLayout = binding.drawerLayout
        recyclerView = binding.chatRecyclerView
        val messageInput = binding.chatMessageInput
        val sendMessageBtn = binding.messageSendBtn
        val backButton = binding.backBtn
        val otherUsername = binding.otherUsername
        membersRecyclerView = binding.membersRecyclerView
        val membersBtn = binding.membersBtn
        leaveGroupButton = binding.leaveGroupButton

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
                viewModel.sendMessage(chatroomId, message)
                messageInput.setText("")
            }
        }

        viewModel.chatRoom.observe(this) { room ->
            chatroomModel = room
            if (room.isGroup) {
                otherUsername.text = room.groupName
                membersBtn.visibility = View.VISIBLE
                if (room.creatorId == FirebaseUtil.currentUserId()) {
                    binding.resendInviteButton.visibility = View.VISIBLE
                } else {
                    binding.resendInviteButton.visibility = View.GONE
                }
                loadGroupMembers()

                FirebaseFirestore.getInstance().collection("events")
                    .whereEqualTo("chatroomId", chatroomId)
                    .get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot.isEmpty) {
                            leaveGroupButton.visibility = View.VISIBLE
                            leaveGroupButton.setOnClickListener {
                                viewModel.leaveGroup(chatroomId) { success ->
                                    Toast.makeText(
                                        this,
                                        if (success) getString(R.string.left_group) else getString(R.string.leave_group_failed),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    if (success) {
                                        val intent = Intent(this, MainActivity::class.java).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        startActivity(intent)
                                        finish()
                                    }
                                }
                            }
                        } else {
                            leaveGroupButton.visibility = View.GONE
                        }
                    }
            } else {
                val otherUserId = room.userIds.first { it != FirebaseUtil.currentUserId() }
                lifecycleScope.launch {
                    repository.getUser(otherUserId)?.let { user ->
                        otherUsername.text = user.username
                    }
                }
                membersBtn.visibility = View.GONE
                leaveGroupButton.visibility = View.GONE
            }
            setupChatRecyclerView()
        }

        viewModel.loadChatRoom(chatroomId, if (::otherUser.isInitialized) otherUser else null)
    }


    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = ChatRecyclerAdapter(options, applicationContext, chatroomModel)

        val manager = LinearLayoutManager(this).apply {
            reverseLayout = true // gi setira porakite vo descending order, poslednoto pishano e najdolu
        }

        recyclerView.layoutManager = manager
        recyclerView.adapter = adapter

        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                recyclerView.smoothScrollToPosition(0)
            }
        })
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
                            Toast.makeText(
                                this,
                                getString(R.string.toast_invitations_resent),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                getString(R.string.toast_failed_resend_invitations),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
    }

    // FirestoreRecyclerAdapter lifecycle handled automatically via setLifecycleOwner
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

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
    }

    override fun onDestroy() {
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
        super.onDestroy()
    }

}
