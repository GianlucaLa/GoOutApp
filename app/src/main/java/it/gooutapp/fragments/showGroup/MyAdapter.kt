package it.gooutapp.fragments.showGroup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.models.Group


class MyAdapter(private val groupList : ArrayList<Group>, val clickListener: ClickListener) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_row, parent, false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {

        val group : Group = groupList[position]
        holder.groupName.text = group.groupName
        val iniziale = group.groupName?.get(0)
        holder.groupPosition.text = "15:46"
        //icona con iniziale
        holder.icon.text = iniziale.toString().toUpperCase()
        holder.itemView.setOnClickListener{
            clickListener.onItemClick(groupList[position])
        }

    }

    override fun getItemCount(): Int {
       return groupList.size
    }

     class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //nome della textView dove inserire dato
        val groupName : TextView = itemView.findViewById(R.id.textViewNomeGruppo)
        val groupPosition : TextView = itemView.findViewById(R.id.textViewOrario)
        val icon : TextView = itemView.findViewById(R.id.textViewDrawable)
    }

    interface ClickListener {
        fun onItemClick(group: Group)
    }
}