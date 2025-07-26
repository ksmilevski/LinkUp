package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.Timestamp
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.FirebaseUtil

class LoginUsernameActivity : AppCompatActivity() {

    private lateinit var usernameInput: EditText
    private lateinit var letMeInBtn: Button
    private lateinit var progressBar: ProgressBar
    private var phoneNumber: String? = null
    private var userModel: UserModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_username)

        usernameInput = findViewById(R.id.login_username)
        letMeInBtn = findViewById(R.id.login_letmein_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        phoneNumber = intent.extras?.getString("phone")
        getUsername()

        letMeInBtn.setOnClickListener {
            setUsername()
        }
    }

    //go setirame nie od poleto shto sme vnele/dobile
    fun setUsername() {
        val username = usernameInput.text.toString()

        if (username.isEmpty() || username.length < 3) {
            usernameInput.error = getString(R.string.error_username_short)
            return
        }

        setInProgress(true)

        //ako vo userModel ima data go setirame samo username, ako nema setirame se
        if (userModel != null) {
            userModel?.username = username
        } else {
            val safeUserId = FirebaseUtil.currentUserId() ?: ""
            userModel = UserModel(phoneNumber ?: "", username, Timestamp.now(), safeUserId)
//            userModel = UserModel(phoneNumber?: "", username, Timestamp.now(), FirebaseUtil.currentUserId())
        }

        //setiraj go userModel i odi na sleden ekran
        FirebaseUtil.currentUserDetails()?.set(userModel!!)
            ?.addOnCompleteListener { task ->
                setInProgress(false)
                if (task.isSuccessful) {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                }
            }
    }


    //go zimame username-ot od firebase database i go stavame vo poleto
    fun getUsername() {
        setInProgress(true)

        FirebaseUtil.currentUserDetails()?.get()?.addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
                val userModel = task.result.toObject(UserModel::class.java) //rezultatot go konvertira vo objekt od UserModel
                userModel?.let {
                    usernameInput.setText(it.username)
                }
            }
        }
    }


    //gi menuva progressBar i kopceto zavisno dali procesot e vo tek
    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            letMeInBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            letMeInBtn.visibility = View.VISIBLE
        }
    }
}