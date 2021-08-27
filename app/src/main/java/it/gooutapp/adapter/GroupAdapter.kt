package it.gooutapp.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.model.Group
import it.gooutapp.model.Notification
import java.time.LocalTime

class GroupAdapter(private val userGroupList: ArrayList<Group>, private val adminFlagList: ArrayList<Boolean>, private val notificationHM: HashMap<String, Notification>, private val clickListener: ClickListener, private val tvEmptyGroupMessage: View) : RecyclerView.Adapter<GroupAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_group, parent, false)
        return MyViewHolder(itemView)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        tvEmptyGroupMessage?.visibility = View.INVISIBLE
        val group: Group = userGroupList[position]
        holder.groupName.text = group.groupName
        val circleTextGroup = group.groupName?.get(0)
        if(adminFlagList[position]) {
            holder.adminFlag.text = "Admin"
        }
        //icona con iniziale
        holder.icon.text = circleTextGroup.toString().toUpperCase()
        holder.itemView.setOnClickListener{
            clickListener.onItemClick(userGroupList[position])
        }
        holder.lastMex.text = notificationHM[group.groupId]?.lastMessage
        val groupNotification = notificationHM[group.groupId]
        //controllo notifiche
        if(groupNotification?.numNotification != null) {
            if(groupNotification?.numNotification!! > 0){
                val time = groupNotification.time?.substring(21, 25)
                holder.time.text = time
                holder.notificationCounter.text = groupNotification.numNotification.toString()
                holder.notificationCounter.visibility = View.VISIBLE
            }
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
        val groupName: TextView = itemView.findViewById(R.id.textViewNomeGruppo)
        val adminFlag: TextView = itemView.findViewById(R.id.textViewAdminFlag)
        val icon: TextView = itemView.findViewById(R.id.textViewDrawable)
        val notificationCounter: TextView = itemView.findViewById(R.id.tvNotificationCounter)
        val lastMex: TextView = itemView.findViewById(R.id.tvMessagePreview)
        val time: TextView = itemView.findViewById(R.id.textViewOrario)
    }

    interface ClickListener {
        fun onItemClick(group: Group)
    }
}