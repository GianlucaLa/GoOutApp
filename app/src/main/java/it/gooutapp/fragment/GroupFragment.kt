package it.gooutapp.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isEmpty
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.adapter.ProposalAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Proposal
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_group.view.*
import java.util.*

class GroupFragment : Fragment(), ProposalAdapter.ClickListenerProposal {
    private val TAG = "GROUP_FRAGMENT"
    private lateinit var recyclerView: RecyclerView
    private lateinit var proposalList: ArrayList<Proposal>
    private lateinit var proposalAdapter: ProposalAdapter
    private val fs = FireStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)
        val groupId = arguments?.get("groupId").toString()

        recyclerView = root.proposalRecycleView
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        proposalList = arrayListOf()
        fs.getProposalData(groupId) { proposalListData ->
            proposalList = proposalListData
            proposalAdapter = ProposalAdapter(proposalList, this)
            recyclerView.adapter = proposalAdapter
            GroupPB.visibility = View.INVISIBLE
        }
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun enterChatListener(proposal: Proposal) {
        val bundle = bundleOf(
            "proposalId" to proposal.proposalId,
            "proposalName" to proposal.proposalName
        )
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_group_to_nav_chat, bundle)
    }

    override fun modifyProposalListener(proposal: Proposal) {
        val bundle = bundleOf(
            "proposalId" to proposal.proposalId,
            "proposalName" to proposal.proposalName,
            "place" to proposal.place,
            "dateTime" to proposal.dateTime,
            "groupId" to proposal.groupId,
            "organizator" to proposal.organizator,
            "organizatorId" to proposal.organizatorId,
            "modify" to true
        )
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_group_to_nav_newProposal, bundle)
    }
}