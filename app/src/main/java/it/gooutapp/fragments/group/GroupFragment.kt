package it.gooutapp.fragments.group

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Proposal
import java.util.ArrayList

class GroupFragment : Fragment() {
    private val TAG = "GROUP_FRAGMENT"
    private lateinit var recyclerView: RecyclerView
    private lateinit var proposalList: ArrayList<Proposal>
    private lateinit var proposalAdapter: ProposalAdapter
    private val fs = FireStore()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)
        setHasOptionsMenu(true)

        recyclerView = root.findViewById(R.id.proposalRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        proposalList = arrayListOf()

        fs.getProposalData() { proposalListData ->
            proposalList = proposalListData
            proposalAdapter = ProposalAdapter(proposalList)
            recyclerView.adapter = proposalAdapter
        }

        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }
}