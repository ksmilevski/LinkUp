package mk.ukim.finki.linkup.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mk.ukim.finki.linkup.R
import mk.ukim.finki.linkup.models.UserModel

class GroupMemberAdapter(
    private val members: List<UserModel>
) : RecyclerView.Adapter<GroupMemberAdapter.MemberViewHolder>() {

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameText: TextView = itemView.findViewById(R.id.user_name_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.group_member_row, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val user = members[position]
        holder.usernameText.text = user.username
    }

    override fun getItemCount(): Int = members.size
}
