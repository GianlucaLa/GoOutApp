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

class ChatAdapter(private val context: Context, private val messageList: ArrayList<Message>) : RecyclerView.Adapter<ChatAdapter.MyViewHolder>() {
    private val TAG = "CHAT_ADAPTER"
    private val MESSAGE_TYPE_LEFT = 0
    private val MESSAGE_TYPE_RIGHT = 1
    var firebaseUser: FirebaseUser? = null

    @SuppressLint("NewApi")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //in base al userId del messaggio, gli assegno il layout destro o sinistro
        return if (viewType == MESSAGE_TYPE_RIGHT) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_right, parent, false)
            Log.e(TAG, view.layoutDirection.toString())
            MyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_left, parent, false)
            //view.setTag(0,"left")
            MyViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val message = messageList[position]
        if(getItemViewType(position) == MESSAGE_TYPE_LEFT) {
            holder.txtUserName.text = Html.fromHtml("<b>${message.ownerNickname}</b><br>${message.text}");
            //holder.txtUserName.text = "${message.ownerNickname}\n${message.text}"
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