package it.gooutapp.adapter

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.MyDialog
import it.gooutapp.model.Proposal
import org.w3c.dom.Text

class ProposalAdapter(private val proposalList: ArrayList<Proposal>, private val clickListenerProposal: ClickListenerProposal, private val tvEmptyProposalMessage: View) : RecyclerView.Adapter<ProposalAdapter.MyViewHolder>() {
    private val TAG = "PROPOSAL_ADAPTER"
    private val fs = FireStore()
    private var curr_user_email = Firebase.auth.currentUser?.email.toString()
    private var user_auth_id = Firebase.auth.currentUser?.uid.toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_proposal_view, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        tvEmptyProposalMessage?.visibility = View.INVISIBLE
        var activityContext = holder.itemView.context
        val proposal: Proposal = proposalList[position]
        holder.nomeProposta.text = proposal.proposalName
        holder.labelPlace.text = "${activityContext.resources.getString(R.string.place)}: "
        holder.labelAddress.text = "${activityContext.resources.getString(R.string.address)}: "
        holder.labelDate.text = "${activityContext.resources.getString(R.string.date)}: "
        holder.labelTime.text = "${activityContext.resources.getString(R.string.time)}: "
        holder.labelOrganizator.text = "${activityContext.resources.getString(R.string.organizator)}: "
        holder.luogoProposta.text = proposal.place.toString()
        holder.indirizzoProposta.text = proposal.placeAddress.toString()
        holder.dataProposta.text = proposal.dateTime.toString().substring(0,10)
        holder.oraProposta.text = proposal.dateTime.toString().substring(11)
        holder.organizzatoreProposta.text = proposal.organizator.toString()
        if (proposal.organizatorId != user_auth_id) {
            holder.btnCancelEvent.visibility = View.GONE
            holder.btnAccept.visibility = View.VISIBLE
            holder.btnRefuse.visibility = View.VISIBLE
        } else {
            holder.btnCancelEvent.visibility = View.VISIBLE
            holder.btnAccept.visibility = View.GONE
            holder.btnRefuse.visibility = View.GONE
        }
        when {
            proposal.accepters?.contains(curr_user_email) == true -> {
                holder.btnAccept.isEnabled = false
            }
            proposal.decliners?.contains(curr_user_email) == true -> {
                holder.btnRefuse.isEnabled = false
            }
            else -> {
                holder.btnAccept.isEnabled = true
                holder.btnRefuse.isEnabled = true
            }
        }
        //listeners
        holder.btnModify.setOnClickListener {
            val wrapper = ContextThemeWrapper(activityContext, R.style.PopupMenu)
            val pop= PopupMenu(wrapper,it)
            pop.inflate(R.menu.proposal_row_menu)
            if(proposal.organizatorId != user_auth_id){
                pop.menu.findItem(R.id.modify).isVisible = false
                pop.menu.findItem(R.id.archives).isVisible = true
            }else{
                pop.menu.findItem(R.id.modify).isVisible = true
                pop.menu.findItem(R.id.archives).isVisible = false
            }
            pop.setOnMenuItemClickListener {item ->
                when(item.itemId) {
                    R.id.modify->{
                        clickListenerProposal.modifyProposalListener(proposalList[position])
                    }
                    R.id.partecipants->{
                        fs.getProposalPartecipants(proposal.creationDate.toString(), activityContext){ participants ->
                            var items = participants.toTypedArray()
                            MaterialAlertDialogBuilder(activityContext)
                                .setTitle(activityContext.resources.getString(R.string.partecipants))
                                .setPositiveButton(R.string.ok){ _, _ ->}
                                .setItems(items){ _, _ ->}
                                .show()
                        }
                    }
                    R.id.archives->{
                        when {
                            holder.btnRefuse.isEnabled -> {
                                Toast.makeText(activityContext, R.string.archive_fail, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                fs.setProposalArchived(proposal.proposalId.toString()){ result ->
                                    if(result){
                                        proposalList.removeAt(position)
                                        notifyItemRemoved(position)
                                        Toast.makeText(activityContext, R.string.proposal_archived, Toast.LENGTH_SHORT).show()
                                    }else{
                                        Toast.makeText(activityContext, R.string.proposal_archived_fail, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
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
                            proposalList.removeAt(position)
                            notifyDataSetChanged()
                            if(proposalList.size == 0)
                                tvEmptyProposalMessage.visibility = View.VISIBLE
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
                            fs.addMessageToChat("accettato", proposal.proposalId.toString())
                            holder.btnAccept.isEnabled = false
                            holder.btnRefuse.isEnabled = true
                            proposalList[position].accepters?.add(curr_user_email)
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
                            fs.addMessageToChat("rifiutato", proposal.proposalId.toString())
                            holder.btnRefuse.isEnabled = false
                            holder.btnAccept.isEnabled = true
                            proposalList[position].decliners?.add(curr_user_email)
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
        val indirizzoProposta: TextView = itemView.findViewById(R.id.textViewRIndirizzoValue)
        val dataProposta: TextView = itemView.findViewById(R.id.textViewRDataValue)
        val oraProposta: TextView = itemView.findViewById(R.id.textViewROraValue)
        val organizzatoreProposta: TextView = itemView.findViewById(R.id.textViewROrganizatorValue)
        val labelPlace: TextView = itemView.findViewById(R.id.textViewRLuogo)
        val labelAddress : TextView = itemView.findViewById(R.id.textViewRIndirizzo)
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