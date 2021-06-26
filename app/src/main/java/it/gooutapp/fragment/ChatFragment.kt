package it.gooutapp.fragment

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import it.gooutapp.R

class ChatFragment: Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false)
        setHasOptionsMenu(true)

        //var recyclerView = root.findViewById<RecyclerView>(R.id.messagesRecycleView)
        //recyclerView.adapter = adapter
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }
}