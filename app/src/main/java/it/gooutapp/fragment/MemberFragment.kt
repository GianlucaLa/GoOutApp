package it.gooutapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import it.gooutapp.R
import it.gooutapp.adapter.MemberAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.User
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
            for ((positon, user) in memberArray.withIndex()) {
                Log.e(TAG, memberArray[positon].nickname.toString())
            }
            memberList = memberArray
            memberAdapter = MemberAdapter(memberList, this)
            recyclerView.adapter = memberAdapter
        }

        return root
    }

    override fun removeMember(user: User) {
        //remove firestore richiamo
    }
}