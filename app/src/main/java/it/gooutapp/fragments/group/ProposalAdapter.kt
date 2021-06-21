package it.gooutapp.fragments.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.models.Proposal

class ProposalAdapter (private val proposalList : ArrayList<Proposal>) : RecyclerView.Adapter<ProposalAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.proposal_view_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val proposal: Proposal = proposalList[position]
        holder.nomeProposta.text = proposal.proposalName
        holder.luogoProposta.text = proposal.place
        val data = proposal.date.toString().substring(0, 13)
        holder.dataProposta.text = data
        val ora = proposal.date?.toString()?.substring(15)
        holder.oraProposta.text = ora
    }

    override fun getItemCount(): Int {
        return proposalList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeProposta : TextView = itemView.findViewById(R.id.textViewRProposta)
        val luogoProposta : TextView = itemView.findViewById(R.id.textViewRLuogo)
        val dataProposta : TextView = itemView.findViewById(R.id.textViewRData)
        val oraProposta : TextView = itemView.findViewById(R.id.textViewROra)
    }
}