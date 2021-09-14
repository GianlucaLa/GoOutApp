package it.gooutapp.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import it.gooutapp.R
import it.gooutapp.adapter.HistoryAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Proposal
import kotlinx.android.synthetic.main.fragment_chat.view.messagesRecyclerView
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_history.view.*

class HistoryFragment : Fragment(), HistoryAdapter.ClickListenerHistory {
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private val fs = FireStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_history, container, false)
        recyclerView = root.messagesRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayoutH)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        loadRecyclerData(root)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            loadRecyclerData(root)
        }

        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadRecyclerData(root: View){
        fs.getUserHistoryProposalData { historyListData ->
            historyAdapter = HistoryAdapter(historyListData,this)
            recyclerView.adapter = historyAdapter
            HistoryPB?.visibility = View.INVISIBLE
            if(historyListData.size == 0){
                val historyMessage = root.tvEmptyHistoryMessage
                historyMessage.text = context?.resources?.getString(R.string.empty_history_message)
                historyMessage?.visibility = View.VISIBLE
            }
        }
    }

    override fun enterChatListener(proposal: Proposal) {
        val bundle = bundleOf(
            "proposalId" to proposal.proposalId,
            "proposalName" to proposal.proposalName
        )
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_history_to_nav_chat, bundle)
    }
}