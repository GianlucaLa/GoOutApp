package it.gooutapp.fragments.showGroup

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group

class ShowGroupFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupArrayList: ArrayList<Group>
    private lateinit var myAdapter: MyAdapter
    private lateinit var db: FirebaseFirestore


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_show_group, container, false)
        val plusButton: FloatingActionButton = root.findViewById(R.id.fab)


        recyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)

        groupArrayList = arrayListOf()

        myAdapter = MyAdapter(groupArrayList)
        recyclerView.adapter = myAdapter
        eventChangeListener()

        plusButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(view.context)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.popup_layout, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editTextPopup)

            with(builder) {
                setTitle(R.string.join_group)
                setPositiveButton(R.string.ok) { dialog, which ->
                    val testo = editText.text.toString()
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

    private fun eventChangeListener() {
        db = FirebaseFirestore.getInstance()
        db.collection("groups").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if(error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }

                for(dc : DocumentChange in value?.documentChanges!!){
                    if(dc.type == DocumentChange.Type.ADDED) {
                        groupArrayList.add(dc.document.toObject(Group::class.java))
                    }
                }
                myAdapter.notifyDataSetChanged()
            }

        })
    }

}