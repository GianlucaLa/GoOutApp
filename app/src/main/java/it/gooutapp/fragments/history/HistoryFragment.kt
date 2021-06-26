package it.gooutapp.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Proposal
import java.util.*

class HistoryFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyList: ArrayList<Proposal>
    private lateinit var historyAdapter: HistoryAdapter
    private val fs = FireStore()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = root.findViewById(R.id.messagesRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        historyList = arrayListOf()

        fs.getUserHistoryProposalData() { historyListData ->
            historyList = historyListData
            historyAdapter = HistoryAdapter(historyList)
            recyclerView.adapter = historyAdapter
        }
        return root
    }
}