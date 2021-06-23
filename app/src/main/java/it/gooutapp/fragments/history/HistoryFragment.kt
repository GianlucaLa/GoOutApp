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
import java.util.ArrayList


class HistoryFragment : Fragment() {
    private val fs = FireStore()
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyList: ArrayList<Proposal>
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        setHasOptionsMenu(true)

        recyclerView = root.findViewById(R.id.historyRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        historyList = arrayListOf()

        fs.getProposalData(arguments?.get("groupCode").toString()) { historyListData ->
            historyList = historyListData
            historyAdapter = HistoryAdapter(historyList)
            recyclerView.adapter = historyAdapter
        }
        return root
    }
}