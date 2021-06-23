package it.gooutapp.fragments.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.models.Proposal

class HistoryAdapter(private val historyList : ArrayList<Proposal>) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>()  {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_view_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val activityContext = holder.itemView.context
        val history: Proposal = historyList[position]
        holder.nomeProposta.text = history.proposalName
        holder.labelPlace.text = "${activityContext.resources.getString(R.string.place)}: "
        holder.labelDate.text = "${activityContext.resources.getString(R.string.date)}: "
        holder.labelTime.text = "${activityContext.resources.getString(R.string.time)}: "
        holder.labelOrganizator.text = "${activityContext.resources.getString(R.string.organizator)}: "
        holder.luogoProposta.text = "${history.place.toString()}"
        holder.dataProposta.text = "${history.date.toString()}"
        holder.oraProposta.text = "${history.time.toString()}"
        holder.organizzatoreProposta.text = "${history.organizator.toString()}"
        //holder.statoProposta.text =  history.state
    }

    override fun getItemCount(): Int {
        return historyList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeProposta: TextView = itemView.findViewById(R.id.textViewHProposta)
        val luogoProposta: TextView = itemView.findViewById(R.id.textViewHLuogoValue)
        val dataProposta: TextView = itemView.findViewById(R.id.textViewHDataValue)
        val oraProposta: TextView = itemView.findViewById(R.id.textViewHOraValue)
        val organizzatoreProposta: TextView = itemView.findViewById(R.id.textViewHOrganizatorValue)
        val statoProposta: TextView = itemView.findViewById(R.id.textViewHStatoProposta)
        val labelPlace: TextView = itemView.findViewById(R.id.textViewHLuogo)
        val labelDate: TextView = itemView.findViewById(R.id.textViewHData)
        val labelTime: TextView = itemView.findViewById(R.id.textViewHOra)
        val labelOrganizator: TextView = itemView.findViewById(R.id.textViewHOrganizator)
    }
}