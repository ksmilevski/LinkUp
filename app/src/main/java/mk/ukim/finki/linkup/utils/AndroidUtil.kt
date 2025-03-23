package mk.ukim.finki.linkup.utils

import android.content.Context
import android.content.Intent
import android.widget.Toast
import mk.ukim.finki.linkup.models.UserModel

class AndroidUtil {
    companion object {
        fun showToast(context: Context, message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }


    fun passUserModelAsIntent(intent: Intent, model: UserModel) {
        intent.putExtra("username", model.username)
        intent.putExtra("phone", model.phone)
        intent.putExtra("userId", model.userId)
        //intent.putExtra("fcmToken", model.fcmToken)
    }

    fun getUserModelFromIntent(intent: Intent): UserModel {
        return UserModel(
            phone = intent.getStringExtra("phone") ?: "",
            username = intent.getStringExtra("username") ?: "",
            userId = intent.getStringExtra("userId") ?: "",
            //fcmToken = intent.getStringExtra("fcmToken") ?: ""
        )
    }

//    fun setProfilePic(context: Context, imageUri: Uri, imageView: ImageView) {
//        Glide.with(context)
//            .load(imageUri)
//            .apply(RequestOptions.circleCropTransform())
//            .into(imageView)
//    }
    }
}
