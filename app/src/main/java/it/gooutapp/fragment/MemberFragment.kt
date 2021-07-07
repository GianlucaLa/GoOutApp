package it.gooutapp.fragment

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.adapter.MemberAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.User
import kotlinx.android.synthetic.main.fragment_member.*
import java.util.ArrayList

class MemberFragment: Fragment(), MemberAdapter.ClickListenerMember {
    private val TAG = "FRAGMENT_MEMBER"
    private lateinit var recyclerView: RecyclerView
    private lateinit var memberList: ArrayList<User>
    private lateinit var memberAdapter: MemberAdapter
    private lateinit var groupId: String
    private val fs = FireStore()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_member, container, false)

        groupId = arguments?.get("groupId").toString()

        recyclerView = root.findViewById(R.id.membersRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        memberList = arrayListOf()
        fs.getGroupMembers(groupId) { memberArray ->
            fs.getGroupAdmin(groupId) { admin ->
                memberList = memberArray
                memberAdapter = MemberAdapter(memberList, admin, this)
                recyclerView.adapter = memberAdapter
                MemberPB?.visibility = View.INVISIBLE
            }
        }
        return root
    }

    override fun removeMember(user: User, position: Int) {
        //TODO funzione su firestore da capire come fare
        memberList.removeAt(position)
        memberAdapter.notifyItemRemoved(position)
    }
}