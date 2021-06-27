package it.gooutapp.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.gooutapp.R
import it.gooutapp.adapter.ChatAdapter
import it.gooutapp.firebase.FireStore
import java.util.ArrayList

class ChatFragment: Fragment() {
    private val TAG = "CHAT_FRAGMENT"
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageList: ArrayList<it.gooutapp.model.Message>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var proposalId: String
    private val fs = FireStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        //acquisisco interfaccia fragment
        val editTextMessage = root.findViewById<EditText>(R.id.editTextChatMessage)
        val btnSendMessage = root.findViewById<FloatingActionButton>(R.id.sendMessageFAB)

        //acquisisco l'id della proposal a cui è associata la chat
        proposalId = arguments?.get("proposalId").toString()

        recyclerView = root.findViewById(R.id.messagesRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        messageList = arrayListOf()
        fs.getChatData(proposalId) { chatArray ->
            messageList = chatArray
            chatAdapter = ChatAdapter(root.context, messageList)
            recyclerView.adapter = chatAdapter
            recyclerView.scrollToPosition(chatAdapter.itemCount -1);
        }

        btnSendMessage.setOnClickListener(){
            var msgText = editTextMessage.text.toString()
            fs.addMessageToChat(msgText, proposalId)
            editTextMessage.setText("")
        }

        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
    }
}