package mk.ukim.finki.linkup

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
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
import com.google.firebase.firestore.Query
import mk.ukim.finki.linkup.models.ChatMessageModel

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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        otherUser = AndroidUtil.getUserModelFromIntent(intent)
        chatroomId = FirebaseUtil.getChatroomId(
            FirebaseUtil.currentUserId() ?: throw IllegalStateException("User ID is null"),
            otherUser.userId
        )

        messageInput = findViewById(R.id.chat_message_input)
        sendMessageBtn = findViewById(R.id.message_send_btn)
        backButton = findViewById(R.id.back_btn)
        otherUsername = findViewById(R.id.other_username)
        recyclerView = findViewById(R.id.chat_recycler_view)

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        otherUsername.setText(otherUser.username)


        sendMessageBtn.setOnClickListener{
            val message = messageInput.text.toString().trim()
            if(message.isEmpty()) {
                return@setOnClickListener
            }
            sendMessageToUser(message)
        }

        getOrCreateChatroomModel()
        setupChatRecyclerView()
    }

    private fun setupChatRecyclerView() {
        val query = FirebaseUtil.getChatroomMessageReference(chatroomId)
            .orderBy("timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatMessageModel>()
            .setQuery(query, ChatMessageModel::class.java)
            .build()

        adapter = ChatRecyclerAdapter(options, applicationContext)

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
    private fun getOrCreateChatroomModel() {
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener { task ->
            if (task.isSuccessful) { //ako e successfull vrateno od firestore
                chatroomModel = task.result?.toObject(ChatRoomModel::class.java) ?: run { //firestore dok go konvertira vo ChatRoomModel object
                    // ako vrati null se izvrsuva run blokot kade sto se kreira nov chatroom so parametri za kosntruktorot
                    ChatRoomModel(
                        chatroomId,
                        listOf(
                            FirebaseUtil.currentUserId(),
                            otherUser.userId
                        ).filterNotNull(), // Ensure list is non-null by filtering out nulls
                        Timestamp.now(),
                        "",
                        ""
                    ).also { FirebaseUtil.getChatroomReference(chatroomId).set(it) } //otkako se kreira chatroom, se zacuvuva vo firestore
                }
            }
        }
    }
}
