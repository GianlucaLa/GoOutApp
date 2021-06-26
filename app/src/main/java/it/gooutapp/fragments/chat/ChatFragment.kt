package it.gooutapp.fragments.chat

import android.os.Bundle
import com.xwray.groupie.Item
import com.xwray.groupie.ViewHolder
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.xwray.groupie.GroupAdapter
import it.gooutapp.R
import kotlinx.android.synthetic.main.fragment_chat.*

class ChatFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        setHasOptionsMenu(true)

        val adapter = GroupAdapter<ViewHolder>()
        adapter.add(ChatItem())
        adapter.add(ChatItem())
        adapter.add(ChatItem())
        adapter.add(ChatItem())

        var recyclerView = root.findViewById<RecyclerView>(R.id.messagesRecycleView)
        recyclerView.adapter = adapter
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }
}

class ChatItem: Item<ViewHolder>(){
    override fun bind(viewHolder: ViewHolder, position: Int) {
    }

    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }
}