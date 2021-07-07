package it.gooutapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.model.Group
import it.gooutapp.model.Proposal
import it.gooutapp.model.User

class MemberAdapter(private val memberList : ArrayList<User>, val clickListenerMember: ClickListenerMember) : RecyclerView.Adapter<MemberAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.member_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val users: User = memberList[position]
        holder.memberName.text = users.nickname
        holder.btnRemove.setOnClickListener {
            clickListenerMember.removeMember(memberList[position])
        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.textViewMemberName)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemoveMember)
    }

    interface ClickListenerMember {
        fun removeMember(user: User)
    }
}