package it.gooutapp.fragments.showGroup

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group


class ShowGroupFragment : Fragment(), MyAdapter.ClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var groupArrayList: ArrayList<Group>
    private lateinit var myAdapter: MyAdapter
    private var user_email = Firebase.auth.currentUser?.email.toString()
    private val fs = FireStore()
    private val TAG = "SHOW_GROUP_FRAGMENT"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_show_group, container, false)
        val createGroupButton: FloatingActionButton = root.findViewById(R.id.fab)

        recyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)
        groupArrayList = arrayListOf()

        fs.getUserGroupData(user_email) { groupList ->
            Log.w(TAG, groupList.toString())
            groupArrayList = groupList
            Log.w(TAG, groupArrayList.toString())
            myAdapter = MyAdapter(groupArrayList, this)
            recyclerView.adapter = myAdapter
            myAdapter.notifyDataSetChanged()

            //swipe a destra recycle view
            val itemSwipe = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,  ItemTouchHelper.LEFT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                   return false
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    //creo background, che in realtà corrisponde allo spazio libero lasciato dalla draw mentre slida a sinistra
                    val background = ColorDrawable(Color.RED)
                    background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                    background.draw(c)
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    showDialog(viewHolder)
                }
            }
            //TODO disabilitare il drag and drop degli elementi della RecyclerView
            //TODO non permettere di evitare la finestra di conferma cancellazione,
            // altrimenti la riga rimane rossa senza item, oppure se perde focus la finestra,
            // rimetto a posto la riga
            val swap = ItemTouchHelper(itemSwipe)
            swap.attachToRecyclerView(recyclerView)
        }

        //Create Group Listener
        createGroupButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(view.context)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.edittext_create_group, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editTextCreateGroup)

            with(builder) {
                setTitle(R.string.create_group)
                setPositiveButton(R.string.ok) { dialog, which ->
                    //rimuovo eventuali spazi vuoti inseriti dall'utente
                    var nomeG = editText.text.toString()
                    if (nomeG != "") {
                        fs.createGroupData(nomeG, user_email) { result ->
                            Toast.makeText(
                                root.context,
                                R.string.group_creation,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(root.context, R.string.error_empty_value, Toast.LENGTH_SHORT)
                            .show()
                    }
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

    //popup per confermare o cancellare row
    private fun showDialog(viewHolder: RecyclerView.ViewHolder) {
        val builder = view?.let { AlertDialog.Builder(it.context) }
        if (builder != null) {
            builder.setTitle(R.string.delete_row)
            builder.setMessage(R.string.delete_row_message)
            builder.setPositiveButton(R.string.ok) {dialog, wich ->
                val position = viewHolder.adapterPosition
                groupArrayList.removeAt(position)
                myAdapter.notifyItemRemoved(position)
            }
            builder.setNegativeButton(R.string.cancel){dialog, wich ->
                val position = viewHolder.adapterPosition
                myAdapter.notifyItemChanged(position)
            }
            builder.show()
        }
    }

//TODO per creare nuovo layout
    override fun onItemClick(group: Group) {

        TODO("Not yet implemented")
    }
}