package it.gooutapp.fragments.history

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Proposal

class HistoryAdapter(private val historyList : ArrayList<Proposal>) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>()  {
    private val fs = FireStore()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.history_view_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val history: Proposal = historyList[position]
        val activityContext = holder.itemView.context
        holder.nomeProposta.text = history.proposalName
        holder.labelPlace.text = "${activityContext.resources.getString(R.string.place)}: "
        holder.labelDate.text = "${activityContext.resources.getString(R.string.date)}: "
        holder.labelTime.text = "${activityContext.resources.getString(R.string.time)}: "
        holder.labelOrganizator.text = "${activityContext.resources.getString(R.string.organizator)}: "
        holder.luogoProposta.text = "${history.place.toString()}"
        holder.dataProposta.text = "${history.dateTime.toString().substring(0,10)}"
        holder.oraProposta.text = "${history.dateTime.toString().substring(11)}"
        holder.organizzatoreProposta.text = "${history.organizator.toString()}"
        fs.getUserProposalState(history.proposalCode.toString()) { proposalState ->
            if(proposalState != "") {
                if (proposalState == "accepted") {
                    holder.statoProposta.setTextColor(activityContext.resources.getColor(R.color.green))
                    holder.statoProposta.text = activityContext.resources.getString(R.string.proposal_accepted)
                    holder.card.setCardBackgroundColor(activityContext.resources.getColor(R.color.greenProposal))
                } else {
                    holder.statoProposta.setTextColor(activityContext.resources.getColor(R.color.lighRed))
                    holder.statoProposta.text = activityContext.resources.getString(R.string.proposal_refused)
                    holder.card.setCardBackgroundColor(activityContext.resources.getColor(R.color.redProposal))
                }
            }else{
                holder.statoProposta.setTextColor(activityContext.resources.getColor(R.color.quantum_grey600))
                holder.statoProposta.text = activityContext.resources.getString(R.string.expired_proposal)
                holder.card.setCardBackgroundColor(activityContext.resources.getColor(R.color.quantum_grey300))
            }
        }
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
        val card: CardView = itemView.findViewById(R.id.historyCV)
    }
}