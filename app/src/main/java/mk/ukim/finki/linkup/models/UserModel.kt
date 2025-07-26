package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

data class UserModel(
    var phone: String = "",
    var username: String = "",
    var createdTimestamp: Timestamp = Timestamp.now(),
    var userId: String = "",
    @get:Exclude @set:Exclude var isSelected: Boolean = false
)