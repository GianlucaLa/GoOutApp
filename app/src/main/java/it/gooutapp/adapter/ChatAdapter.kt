package it.gooutapp.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Html
import android.util.Log
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

class ChatAdapter(private val context: Context, private val messageList: ArrayList<Message>, private val tvEmptyMessage: View) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private val TAG = "CHAT_ADAPTER"
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = null

    @SuppressLint("NewApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //in base al userId del messaggio, gli assegno il layout destro o sinistro
        if (viewType == MESSAGE_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_right, parent, false)
            Log.e(TAG, view.layoutDirection.toString())
            return MyViewHolder(view)
        } else if (viewType == MESSAGE_TYPE_LEFT){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_left, parent, false)
            return MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_center, parent, false)
            return MyViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        tvEmptyMessage?.visibility = View.INVISIBLE
        val message = messageList[position]
        if(getItemViewType(position) == MESSAGE_TYPE_LEFT) {
            holder.txtUserName.text = Html.fromHtml("<b>${message.ownerNickname}</b><br>${message.text}");
        } else {
            holder.txtUserName.text = "${message.text}"
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtUserName: TextView = itemView.findViewById(R.id.tvMessage)
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        return if (messageList[position].owner == firebaseUser!!.uid) {
            MESSAGE_TYPE_RIGHT
        } else {
            MESSAGE_TYPE_LEFT
        }
    }
}