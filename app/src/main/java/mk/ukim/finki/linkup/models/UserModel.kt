package mk.ukim.finki.linkup.models

import com.google.firebase.Timestamp

data class UserModel (

    var phone: String = "",
    var username: String = "",
    var createdTimestamp: Timestamp = Timestamp.now(),
    var userId: String = "",
    var isSelected: Boolean = false // <-- NEW


//    constructor(createdTimestamp: Timestamp, username: String, phone: String) {
//        this.createdTimestamp = createdTimestamp
//        this.username = username
//        this.phone = phone
//    }
){
    constructor(createdTimestamp: Timestamp, username: String, phone: String, userId: String)
            : this(phone, username, createdTimestamp, userId)
}