package it.gooutapp.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.model.Proposal

class HistoryAdapter(private val historyList : ArrayList<Proposal>, val clickListenerHistory: ClickListenerHistory) : RecyclerView.Adapter<HistoryAdapter.MyViewHolder>()  {
    private var curr_user_email = Firebase.auth.currentUser?.email.toString()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.row_history_view, parent, false)
        return MyViewHolder(itemView)
    }

    @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val history: Proposal = historyList[position]
        val activityContext = holder.itemView.context
        holder.nomeProposta.text = history.proposalName
        holder.labelPlace.text = "${activityContext.resources.getString(R.string.place)}: "
        holder.labelDate.text = "${activityContext.resources.getString(R.string.date)}: "
        holder.labelTime.text = "${activityContext.resources.getString(R.string.time)}: "
        holder.labelNomeGruppo.text ="${activityContext.resources.getString(R.string.groupName)}: "
        holder.labelOrganizator.text = "${activityContext.resources.getString(R.string.organizator)}: "
        holder.luogoProposta.text = history.place.toString()
        holder.dataProposta.text = history.dateTime.toString().substring(0,10)
        holder.oraProposta.text = history.dateTime.toString().substring(11)
        holder.organizzatoreProposta.text = history.organizator.toString()
        holder.nomeGruppo.text = history.groupName.toString()
        holder.btnChat.setOnClickListener {
            clickListenerHistory.enterChatListener(historyList[position])
        }
        if (history.canceled == "canceled") {
            holder.statoProposta.text = activityContext.resources.getString(R.string.canceled)
            holder.statoProposta.background = activityContext.resources.getDrawable(R.drawable.background_canceled)
        } else if(history.accepters?.contains(curr_user_email) == true || history.decliners?.contains(curr_user_email) == true) {
            if (history.accepters?.contains(curr_user_email) == true) {
                holder.statoProposta.background = activityContext.resources.getDrawable(R.drawable.background_accepted)
                holder.statoProposta.text = activityContext.resources.getString(R.string.proposal_accepted)
            } else {
                holder.statoProposta.background = activityContext.resources.getDrawable(R.drawable.background_refused)
                holder.statoProposta.text = activityContext.resources.getString(R.string.proposal_refused)
            }
        }else{
            holder.statoProposta.background = activityContext.resources.getDrawable(R.drawable.background_expired)
            holder.statoProposta.text = activityContext.resources.getString(R.string.expired_proposal)
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
        val statoProposta: TextView = itemView.findViewById(R.id.textViewHStatoValue)
        val labelPlace: TextView = itemView.findViewById(R.id.textViewHLuogo)
        val labelDate: TextView = itemView.findViewById(R.id.textViewHData)
        val labelTime: TextView = itemView.findViewById(R.id.textViewHOra)
        val labelOrganizator: TextView = itemView.findViewById(R.id.textViewHorganizator)
        val btnChat : Button = itemView.findViewById(R.id.entryChatHistory)
        val nomeGruppo: TextView = itemView.findViewById(R.id.textViewHGroupNameValue)
        val labelNomeGruppo: TextView = itemView.findViewById(R.id.textViewHGroupName)
    }

    interface ClickListenerHistory {
        fun enterChatListener(proposal: Proposal)
    }
}