package it.gooutapp.fragments.showGroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group

class ShowGroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupArrayList: ArrayList<Group>
    private lateinit var myAdapter: MyAdapter
    private var user_email = Firebase.auth.currentUser?.email.toString()
    private val fs = FireStore()
    private val TAG = "SHOW_GROUP_FRAGMENT"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_show_group, container, false)
        val searchButton: FloatingActionButton = root.findViewById(R.id.fab)


        recyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)
        groupArrayList = arrayListOf()

        fs.getGroupData(user_email){ groupList ->
            Log.w(TAG, groupList.toString())
            groupArrayList = groupList
            Log.w(TAG, groupArrayList.toString())
            myAdapter = MyAdapter(groupArrayList)
            recyclerView.adapter = myAdapter
            myAdapter.notifyDataSetChanged()
        }

        //Search Group Listener
        searchButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(view.context)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.edittext_search_group, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editTextJoinGroup)

            with(builder) {
                setTitle(R.string.create_group)
                setPositiveButton(R.string.ok) { dialog, which ->
                    val testo = editText?.text?.toString()
                }
                setNegativeButton(R.string.cancel) { dialog, which ->
                    //null operation
                }
                setView(dialogLayout)
                show()
            }
        }
        return root
    }
}