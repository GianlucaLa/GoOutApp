package it.gooutapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.model.User

class MemberAdapter(private val memberList : ArrayList<User>, private val admin: String, val clickListenerMember: ClickListenerMember) : RecyclerView.Adapter<MemberAdapter.MyViewHolder>() {
    private val TAG = "MEMBER_ADAPTER"
    private var currentUserEmail = Firebase.auth.currentUser?.email.toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.member_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var activityContext = holder.itemView.context
        var thisUser: User = memberList[position]
        Log.e(TAG, thisUser.email.toString())
        holder.memberName.text = thisUser.nickname
        if(admin == thisUser.email && thisUser.email == currentUserEmail)
            holder.you.text = "${activityContext.resources.getString(R.string.you)}"
        else if(thisUser.email == currentUserEmail)
            holder.you.text = "${activityContext.resources.getString(R.string.you)}"
        else
            holder.memberName.text = thisUser.nickname
        if (admin != Firebase.auth.currentUser?.email.toString() || admin == thisUser.email){
            holder.btnRemove.visibility = View.GONE
        } else {
            holder.btnRemove.setOnClickListener {
                clickListenerMember.removeMember(memberList[position], position)
            }
        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.textViewMemberName)
        val btnRemove: Button = itemView.findViewById(R.id.btnRemoveMember)
        val you: TextView = itemView.findViewById(R.id.textViewYou)
    }

    interface ClickListenerMember {
        fun removeMember(user: User,position: Int)
    }
}