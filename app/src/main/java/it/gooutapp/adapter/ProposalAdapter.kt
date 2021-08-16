package it.gooutapp.adapter

import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.MyDialog
import it.gooutapp.model.Proposal

class ProposalAdapter(private val proposalList: ArrayList<Proposal>, val clickListenerProposal: ClickListenerProposal) : RecyclerView.Adapter<ProposalAdapter.MyViewHolder>() {
    private val TAG = "PROPOSAL_ADAPTER"
    private val fs = FireStore()
    private var user_auth_id = Firebase.auth.currentUser?.uid.toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_proposal_view, parent, false)
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
        holder.dataProposta.text = "${proposal.dateTime.toString().substring(0,10)}"
        holder.oraProposta.text = "${proposal.dateTime.toString().substring(11)}"
        holder.organizzatoreProposta.text = "${proposal.organizator.toString()}"
        if (proposal.organizatorId != user_auth_id) {
            holder.btnCancelEvent.visibility = View.GONE
        } else {
            holder.btnAccept.visibility = View.GONE
            holder.btnRefuse.visibility = View.GONE
        }
        holder.btnModify.setOnClickListener {
            val wrapper = ContextThemeWrapper(activityContext, R.style.PopupMenu)
            val pop= PopupMenu(wrapper,it)
            pop.inflate(R.menu.proposal_row_menu)
            pop.setOnMenuItemClickListener {item->
                when(item.itemId) {
                    R.id.modify->{clickListenerProposal.modifyProposalListener(proposalList[position])}
                    R.id.partecipants->{
                        fs.getProposalPartecipants(proposal.proposalId.toString()){ participants ->
                            var items = if(participants.size != 0){
                                Log.e(TAG, participants.size.toString())
                                participants.toTypedArray() as Array<CharSequence>
                            }else{
                                Log.e(TAG, participants.size.toString())
                                arrayOf(activityContext.resources.getString(R.string.no_partecipant))
                            }
                            MaterialAlertDialogBuilder(activityContext)
                                .setTitle(activityContext.resources.getString(R.string.partecipants))
                                .setPositiveButton(R.string.ok){_, _ ->}
                                .setItems(items){ dialog, which ->

                                }
                                .show()
                        }
                    }
                }
                true
            }
            pop.show()
            true
        }
        holder.btnCancelEvent.setOnClickListener(){
            val title = activityContext.resources.getString(R.string.cancel_event)
            val message = activityContext.resources.getString(R.string.cancel_event_message)
            MyDialog(title, message, activityContext){ confirm ->
                if(confirm){
                    fs.cancelProposal(proposal.proposalId.toString()){ result->
                        if(result){
                            Toast.makeText(activityContext, R.string.proposal_state_successful, Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(activityContext, R.string.proposal_state_fail, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        holder.btnAccept.setOnClickListener(){
            val title = activityContext.resources.getString(R.string.proposal_accept_title_popup)
            val message = activityContext.resources.getString(R.string.proposal_accept_message_popup)
            MyDialog(title, message, activityContext){ confirm ->
                if(confirm){
                    fs.setProposalState(proposal.proposalId.toString(), "accepted"){ result->
                        if(result){
                            Toast.makeText(activityContext, R.string.proposal_state_successful, Toast.LENGTH_SHORT).show()
                        }else {
                            Toast.makeText(activityContext, R.string.proposal_state_fail, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        holder.btnRefuse.setOnClickListener(){
            val title = activityContext.resources.getString(R.string.proposal_decline_title_popup)
            val message = activityContext.resources.getString(R.string.proposal_decline_message_popup)
            MyDialog(title, message, activityContext){ confirm ->
                if(confirm){
                    fs.setProposalState(proposal.proposalId.toString(), "refused"){ result->
                        if(result){
                            Toast.makeText(activityContext, R.string.proposal_state_successful, Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(activityContext, R.string.proposal_state_fail, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
        holder.btnChat.setOnClickListener {
            clickListenerProposal.enterChatListener(proposalList[position])
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
        val btnChat: Button = itemView.findViewById(R.id.entryChat)
        val btnModify: Button = itemView.findViewById(R.id.modifyProposal)
        val btnCancelEvent: Button = itemView.findViewById(R.id.cancelEvent)
    }

    interface ClickListenerProposal {
        fun enterChatListener(proposal: Proposal)
        fun modifyProposalListener(proposal: Proposal)
    }
}