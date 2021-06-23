package it.gooutapp.fragments.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.models.Group

class GroupAdapter(private val userGroupList : ArrayList<Group>, val adminFlagList : ArrayList<Boolean>, val clickListener: ClickListener) : RecyclerView.Adapter<GroupAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val user: Group = userGroupList[position]
        holder.groupName.text = user.groupName
        val iniziale = user.groupName?.get(0)
        holder.groupPosition.text = "15:46"
        if(adminFlagList[position]) {
            holder.adminFlag.text = "Admin"
        }
        //icona con iniziale
        holder.icon.text = iniziale.toString().toUpperCase()
        holder.itemView.setOnClickListener{
            clickListener.onItemClick(userGroupList[position])
        }
    }

    fun deleteItemRow(position: Int){
        adminFlagList.removeAt(position)
    }

    override fun getItemCount(): Int {
       return userGroupList.size
    }

     class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //nome della textView dove inserire dato
        val groupName : TextView = itemView.findViewById(R.id.textViewNomeGruppo)
        val adminFlag : TextView = itemView.findViewById(R.id.textViewAdminFlag)
        val groupPosition : TextView = itemView.findViewById(R.id.textViewOrario)
        val icon : TextView = itemView.findViewById(R.id.textViewDrawable)
    }

    interface ClickListener {
        fun onItemClick(group: Group)
    }
}