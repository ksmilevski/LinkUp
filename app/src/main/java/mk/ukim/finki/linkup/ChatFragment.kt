package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Filter
import mk.ukim.finki.linkup.adapter.RecentChatRecyclerAdapter
import mk.ukim.finki.linkup.models.ChatRoomModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class ChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentChatRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        recyclerView = view.findViewById(R.id.recyler_view)
        setupRecyclerView()

        val createGroupBtn = view.findViewById<Button>(R.id.create_group_btn)
        createGroupBtn.setOnClickListener {
            val intent = Intent(requireContext(), CreateGroupActivity::class.java)
            startActivity(intent)
        }
        val createEventBtn = view.findViewById<Button>(R.id.create_event_btn)
        createEventBtn.setOnClickListener {
            val intent = Intent(context, CreateEventActivity::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun setupRecyclerView() {
        val currentId = FirebaseUtil.currentUserId() ?: ""
        val query = FirebaseUtil.allChatroomCollectionReference()
            .whereArrayContains("userIds", currentId)
            .where(
                Filter.or(
                    Filter.equalTo("group", true),
                    Filter.greaterThan("lastMessageTimestamp", Timestamp(0, 0))
                )
            )
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<ChatRoomModel>()
            .setQuery(query, ChatRoomModel::class.java)
            .build()

        adapter = RecentChatRecyclerAdapter(options, requireContext())
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        adapter.startListening()
    }

    override fun onStart() {
        super.onStart()
        if (::adapter.isInitialized) {
            adapter.startListening()
        }
    }

    override fun onStop() {
        super.onStop()
        if (::adapter.isInitialized) {
            adapter.stopListening()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
    }
}
