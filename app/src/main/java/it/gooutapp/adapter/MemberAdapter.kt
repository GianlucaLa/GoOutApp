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

class MemberAdapter(private val memberList : ArrayList<Proposal>, val clickListenerMember: ClickListenerMember) : RecyclerView.Adapter<MemberAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.member_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val memberList: Proposal = memberList[position]
        //holder.memberName.text = memberList.
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberName: TextView = itemView.findViewById(R.id.textViewNomeMembro)
        val btnRemove: Button = itemView.findViewById(R.id.buttonRemoveMember)
    }

    interface ClickListenerMember {
        //fare se serve clickListener
    }
}