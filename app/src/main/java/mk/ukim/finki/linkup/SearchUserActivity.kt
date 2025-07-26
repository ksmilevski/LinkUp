package mk.ukim.finki.linkup

import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import mk.ukim.finki.linkup.adapter.SearchUserRecyclerAdapter
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.FirebaseUtil


class SearchUserActivity : AppCompatActivity() {

    private lateinit var searchInput: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchUserRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_user)


        searchInput = findViewById(R.id.search_username_input)
        searchButton = findViewById(R.id.search_user_btn)
        backButton = findViewById(R.id.back_btn)
        recyclerView = findViewById(R.id.search_user_recycler_view)

        //za koa ke se otvori ova activity da e fokusirano tuka
        searchInput.requestFocus()

        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        searchButton.setOnClickListener {
            val searchTerm = searchInput.text.toString()
            if (searchTerm.isEmpty() || searchTerm.length < 3) {
                searchInput.error = getString(R.string.error_invalid_username)
                return@setOnClickListener
            }
            setupSearchRecyclerView(searchTerm)
        }

    }

    //za prikaz na korisnicite vo rec view vrz osnova na vneseno korisnicko ime
    fun setupSearchRecyclerView(searchTerm: String) {
        val query = FirebaseUtil.allUserCollectionReference()
            .whereGreaterThanOrEqualTo("username", searchTerm) //site useri so toj username so ke go vnesime
            .whereLessThanOrEqualTo("username", searchTerm + '\uf8ff')

        val options = FirestoreRecyclerOptions.Builder<UserModel>()
            .setQuery(query, UserModel::class.java)
            .setLifecycleOwner(this)
            .build()

        adapter = SearchUserRecyclerAdapter(options, applicationContext)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    // FirestoreRecyclerAdapter lifecycle is handled via setLifecycleOwner

}