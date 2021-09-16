package it.gooutapp.adapter

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
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_member, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var activityContext = holder.itemView.context
        var thisUser: User = memberList[position]
        holder.memberName.text = thisUser.nickname

        if(thisUser.email == currentUserEmail)
            holder.you.text = "${activityContext.resources.getString(R.string.you)}"
        else
            holder.memberName.text = thisUser.nickname

        if(currentUserEmail != admin && thisUser.email == admin) {
            holder.you.setTextColor(holder.colorAdmin)
            holder.you.text = "Admin"
        }
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
        val colorAdmin = itemView.resources.getColor(R.color.gold)
    }

    interface ClickListenerMember {
        fun removeMember(user: User,position: Int)
    }
}