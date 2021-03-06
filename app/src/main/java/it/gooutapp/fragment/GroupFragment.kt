package it.gooutapp.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.adapter.ProposalAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Proposal
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_group.view.*
import kotlin.properties.Delegates

class GroupFragment : Fragment(), ProposalAdapter.ClickListenerProposal {
    private val TAG = "GROUP_FRAGMENT"
    private var curr_user_email = Firebase.auth.currentUser?.email.toString()
    private lateinit var groupId: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var proposalAdapter: ProposalAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var activityContext: Context
    private val fs = FireStore()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)
        activityContext = root.context
        groupId = arguments?.get("groupId").toString()
        val linearLayoutManager = LinearLayoutManager(root.context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerView = root.proposalRecyclerView
        recyclerView.layoutManager = linearLayoutManager
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        loadRecyclerData(root)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            loadRecyclerData(root)
        }
        setHasOptionsMenu(true)
        return root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadRecyclerData(root: View){
        fs.getGroupFragmentProposalData(groupId) { proposalListData ->
            //Blocco abilita textview fragment vuoto
            val emptyProposalMessage = root.tvEmptyProposalMessage
            emptyProposalMessage.text = context?.resources?.getString(R.string.empty_proposal_message)
            proposalAdapter = ProposalAdapter(proposalListData, this, emptyProposalMessage)
            recyclerView.adapter = proposalAdapter
            GroupPB?.visibility = View.INVISIBLE
            if(proposalListData.isEmpty()){
                tvEmptyProposalMessage?.visibility = View.VISIBLE
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu)
        fs.getGroupPopupNotification(activityContext, groupId) { notificationList ->
            if (notificationList.isNotEmpty())
                menu.findItem(R.id.notification).icon = activityContext.getDrawable(R.drawable.ic_new_notification)
            fs.getGroupAdmin(groupId) { admin ->
                if (admin == Firebase.auth.currentUser?.email.toString()) {
                    var groupCodeItem = menu.findItem(R.id.group_code_item)
                    groupCodeItem?.isVisible = true
                }
            }
            super.onCreateOptionsMenu(menu, inflater)
        }
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
            "creationDate" to proposal.creationDate,
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