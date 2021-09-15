package it.gooutapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import it.gooutapp.R
import it.gooutapp.model.Message
import java.util.*

class ChatAdapter(private val context: Context, private val messageList: ArrayList<Message>, private val tvEmptyMessage: View, private val proposalId: String) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private val TAG = "CHAT_ADAPTER"
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    private val MESSAGE_TYPE_CENTER = 2
    var firebaseUser: FirebaseUser? = null

    @SuppressLint("NewApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return when (viewType) {
            MESSAGE_TYPE_RIGHT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_right, parent, false)
                MyViewHolder(view)
            }
            MESSAGE_TYPE_LEFT -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_left, parent, false)
                MyViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_center, parent, false)
                MyViewHolder(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        tvEmptyMessage?.visibility = View.INVISIBLE
        val message = messageList[position]
        if(getItemViewType(position) == MESSAGE_TYPE_LEFT) {
            holder.txtUserName.text = Html.fromHtml("<b>${message.nickname}</b><br>${message.text}")
        } else if (getItemViewType(position) == MESSAGE_TYPE_RIGHT){
            holder.txtUserName.text = "${message.text}"
        } else {
            if (message.text == "accettato")
                holder.txtUserName.text = "${message.nickname } ${context.resources.getString(R.string.chat_accepted_message)}"
            else
                holder.txtUserName.text = "${message.nickname } ${context.resources.getString(R.string.chat_refused_message)}"
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (messageList[position].user_id == firebaseUser!!.uid && !(messageList[position].text == "accettato" || messageList[position].text == "rifiutato")) {
            MESSAGE_TYPE_RIGHT
        } else if (messageList[position].user_id != firebaseUser!!.uid && !(messageList[position].text == "accettato" || messageList[position].text == "rifiutato")){
            MESSAGE_TYPE_LEFT
        } else{
            MESSAGE_TYPE_CENTER
        }
    }
}