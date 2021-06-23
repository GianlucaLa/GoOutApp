package it.gooutapp.fragments.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Proposal

class ProposalAdapter (private val proposalList : ArrayList<Proposal>) : RecyclerView.Adapter<ProposalAdapter.MyViewHolder>() {
    private val fs = FireStore()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.proposal_view_row, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        var activityContext = holder.itemView.context
        val proposal: Proposal = proposalList[position]
        holder.nomeProposta.text = proposal.proposalName
        holder.labelPlace.text = "${activityContext.resources.getString(R.string.place)}: "
        holder.labelDate.text = "${activityContext.resources.getString(R.string.date)}: "
        holder.labelTime.text = "${activityContext.resources.getString(R.string.time)}: "
        holder.labelOrganizator.text = "${activityContext.resources.getString(R.string.organizator)}: "
        holder.luogoProposta.text = "${proposal.place.toString()}"
        holder.dataProposta.text = "${proposal.date.toString()}"
        holder.oraProposta.text = "${proposal.time.toString()}"
        holder.organizzatoreProposta.text = "${proposal.organizator.toString()}"
        holder.btnAccept.setOnClickListener(){
            fs.setProposalState(proposal.proposalCode.toString(), "accepted"){ result->
                if(result){
                    Toast.makeText(activityContext, R.string.proposal_state_successful, Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }else{
                    Toast.makeText(activityContext, R.string.proposal_state_fail, Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()
                }
            }
        }
        holder.btnRefuse.setOnClickListener(){
            fs.setProposalState(proposal.proposalCode.toString(), "refused"){ result->
                if(result){
                    Toast.makeText(activityContext, R.string.proposal_state_successful, Toast.LENGTH_SHORT).show()
                }else{
                    Toast.makeText(activityContext, R.string.proposal_state_fail, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return proposalList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomeProposta: TextView = itemView.findViewById(R.id.textViewRProposta)
        val luogoProposta: TextView = itemView.findViewById(R.id.textViewRLuogoValue)
        val dataProposta: TextView = itemView.findViewById(R.id.textViewRDataValue)
        val oraProposta: TextView = itemView.findViewById(R.id.textViewROraValue)
        val organizzatoreProposta: TextView = itemView.findViewById(R.id.textViewROrganizatorValue)
        val labelPlace: TextView = itemView.findViewById(R.id.textViewRLuogo)
        val labelDate: TextView = itemView.findViewById(R.id.textViewRData)
        val labelTime: TextView = itemView.findViewById(R.id.textViewROra)
        val labelOrganizator: TextView = itemView.findViewById(R.id.textViewOrganizator)
        val btnAccept: Button = itemView.findViewById(R.id.acceptProposal)
        val btnRefuse: Button = itemView.findViewById(R.id.refuseProposal)
    }
}