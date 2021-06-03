package it.gooutapp.fragments.showGroup

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.models.Group


class MyAdapter(private val groupList : ArrayList<Group>) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyAdapter.MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.recycle_view_row, parent, false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyAdapter.MyViewHolder, position: Int) {


        val group : Group = groupList[position]
        holder.groupName.text = group.groupName
        val iniziale = group.groupName?.get(0)
        holder.groupPosition.text = "${position+1}"
        //icona con iniziale
        holder.icon.text = iniziale.toString().toUpperCase()

    }

    override fun getItemCount(): Int {

       return groupList.size
    }

    public class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //nome della textView dove inserire dato
        val groupName : TextView = itemView.findViewById(R.id.textViewNomeGruppo)
        val groupPosition : TextView = itemView.findViewById(R.id.textViewNumGruppo)
        val icon : TextView = itemView.findViewById(R.id.textViewDrawable)
    }
}