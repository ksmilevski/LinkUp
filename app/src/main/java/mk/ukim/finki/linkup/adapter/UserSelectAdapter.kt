package mk.ukim.finki.linkup.mk.ukim.finki.linkup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.linkup.R
import mk.ukim.finki.linkup.models.UserModel

class UserSelectAdapter(
    private val userList: List<UserModel>,
    private val onUserChecked: (UserModel, Boolean) -> Unit
) : RecyclerView.Adapter<UserSelectAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
        val checkbox: CheckBox = itemView.findViewById(R.id.user_checkbox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user_checkbox, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.usernameText.text = user.username
        holder.checkbox.isChecked = user.isSelected

        holder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            user.isSelected = isChecked
            onUserChecked(user, isChecked)
        }
    }

    override fun getItemCount(): Int = userList.size
}
