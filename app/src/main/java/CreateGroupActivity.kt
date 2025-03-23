package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.utils.FirebaseUtil
import com.google.firebase.Timestamp
import mk.ukim.finki.linkup.mk.ukim.finki.linkup.adapter.UserSelectAdapter
import mk.ukim.finki.linkup.models.UserModel
import java.util.UUID

class CreateGroupActivity : AppCompatActivity() {

    private lateinit var groupNameInput: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var createGroupButton: Button
    private val userList = mutableListOf<UserModel>()
    private val selectedUserIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        groupNameInput = findViewById(R.id.group_name_input)
        recyclerView = findViewById(R.id.users_recycler_view)
        createGroupButton = findViewById(R.id.create_group_btn)

        fetchUsers()

        createGroupButton.setOnClickListener {
            val groupName = groupNameInput.text.toString().trim()
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Enter group name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedUserIds.isEmpty()) {
                Toast.makeText(this, "Select at least one user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createGroupChat(selectedUserIds, groupName)
        }
    }

    private fun fetchUsers() {
        FirebaseUtil.allUserCollectionReference().get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (doc in result.documents) {
                    val user = doc.toObject(UserModel::class.java)
                    val currentId = FirebaseUtil.currentUserId()
                    if (user != null && user.userId != currentId) {
                        userList.add(user)
                    }
                }
                setupRecyclerView()
            }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UserSelectAdapter(userList) { user, isChecked ->
            if (isChecked) {
                selectedUserIds.add(user.userId)
            } else {
                selectedUserIds.remove(user.userId)
            }
        }
    }

    private fun createGroupChat(selectedUserIds: List<String>, groupName: String) {
        val chatroomId = UUID.randomUUID().toString()

        val allUserIds = selectedUserIds.toMutableList()
        FirebaseUtil.currentUserId()?.let {
            if (!allUserIds.contains(it)) allUserIds.add(it)
        }

        val chatroom = ChatRoomModel(
            chatroomId = chatroomId,
            userIds = allUserIds,
            isGroup = true,
            groupName = groupName,
            lastMessage = "",
            lastMessageSenderId = "",
            lastMessageTimestamp = Timestamp.now()
        )

        FirebaseUtil.getChatroomReference(chatroomId).set(chatroom)
            .addOnSuccessListener {
                Toast.makeText(this, "Group created!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("chatroomId", chatroomId)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show()
            }
    }
}

