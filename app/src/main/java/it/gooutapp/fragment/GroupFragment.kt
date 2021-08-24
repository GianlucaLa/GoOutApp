package it.gooutapp.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.adapter.ProposalAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Proposal
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_group.view.*

class GroupFragment : Fragment(), ProposalAdapter.ClickListenerProposal {
    private val TAG = "GROUP_FRAGMENT"
    private lateinit var groupId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var proposalAdapter: ProposalAdapter
    private val fs = FireStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)
        groupId = arguments?.get("groupId").toString()
        recyclerView = root.proposalRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(root.context)

        fs.getProposalData(groupId) { proposalListData ->
            proposalAdapter = ProposalAdapter(proposalListData, this, tvEmptyProposalMessage)
            recyclerView.adapter = proposalAdapter
            GroupPB?.visibility = View.INVISIBLE
            if(proposalListData.size == 0){
                tvEmptyProposalMessage?.visibility = View.VISIBLE
            }
        }

        setHasOptionsMenu(true)
        return root
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu)
        fs.getGroupAdmin(groupId){  admin ->
            if (admin == Firebase.auth.currentUser?.email.toString()) {
                var groupCodeItem = menu?.findItem(R.id.group_code_item)
                groupCodeItem?.isVisible = true
            }
        }
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
            "groupName" to proposal.groupName,
            "organizator" to proposal.organizator,
            "organizatorId" to proposal.organizatorId,
            "modify" to true
        )
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_group_to_nav_newProposal, bundle)
    }
}