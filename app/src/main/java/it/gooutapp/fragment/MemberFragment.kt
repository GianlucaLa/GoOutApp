package it.gooutapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    private lateinit var root: View
    private val fs = FireStore()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_member, container, false)

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
        fs.removeMemberGroup(groupId, user.authId.toString()){ result ->
            if (result) {
                memberList.removeAt(position)
                memberAdapter.notifyItemRemoved(position)
                Toast.makeText(root.context, R.string.successfulRemoveMember, Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(root.context, R.string.failRemoveMember, Toast.LENGTH_SHORT).show()
            }
        }

    }
}