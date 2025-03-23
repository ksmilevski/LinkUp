package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import mk.ukim.finki.linkup.models.UserModel
import mk.ukim.finki.linkup.utils.AndroidUtil
import mk.ukim.finki.linkup.utils.FirebaseUtil


class ProfileFragment : Fragment() {

    lateinit var profilePic: ImageView
    lateinit var usernameInput: EditText
    lateinit var phoneInput: EditText
    lateinit var updateProfileBtn: Button
    lateinit var progressBar: ProgressBar
    lateinit var logoutBtn: TextView
    lateinit var currentUserModel: UserModel
    var imagePickLauncher: ActivityResultLauncher<Intent>? = null //a newer way to handle activity results in a more robust and lifecycle-aware manner



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var view = inflater.inflate(R.layout.fragment_profile, container, false)
        profilePic = view.findViewById(R.id.profile_image_view)
        usernameInput = view.findViewById(R.id.profile_username)
        phoneInput = view.findViewById(R.id.profile_phone)
        updateProfileBtn = view.findViewById(R.id.profile_update_btn)
        progressBar = view.findViewById(R.id.profile_progress_bar)
        logoutBtn = view.findViewById(R.id.logout_btn)

        getUserData()

        updateProfileBtn.setOnClickListener {
            updateBtnClick()
        }

        logoutBtn.setOnClickListener{
            FirebaseUtil.logout()
            val intent = Intent(requireContext(), SplashActivity::class.java)
            //site aktivitija ke bidat izbrishani od stack, samo novoto aktivity ke ostane
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or  Intent.FLAG_ACTIVITY_CLEAR_TASK )
            startActivity(intent)
        }

        return view
    }

    fun updateBtnClick() {
        val newUsername = usernameInput.text.toString()

        //validacija
        if (newUsername.isEmpty() || newUsername.length < 3) {
            usernameInput.error = "Username length should be at least 3 chars"
            return
        }
        currentUserModel.username = newUsername
        setInProgress(true)
        updateToFirestore()
    }

    fun updateToFirestore() {
        FirebaseUtil.currentUserDetails()?.set(currentUserModel)
            ?.addOnCompleteListener { task ->
                setInProgress(false)
                val message = if (task.isSuccessful) "Updated successfully" else "Update failed"
                context?.let { AndroidUtil.showToast(it, message) }
            }
    }


    //od firebase se zimat informaciite za userot
    fun getUserData() {
        setInProgress(true)
        FirebaseUtil.currentUserDetails()?.get()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document =
                    task.result //vo task.result e DocumentSnapshot (Firestore doc od bazata), t.e datata/info od bazata
                if (document != null && document.exists()) {
                    setInProgress(false)
                    currentUserModel = document.toObject(UserModel::class.java) ?: UserModel()
                    usernameInput.setText(currentUserModel?.username)
                    phoneInput.setText(currentUserModel?.phone)
                }
            }
        }
    }

    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            updateProfileBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            updateProfileBtn.visibility = View.VISIBLE
        }
    }


}