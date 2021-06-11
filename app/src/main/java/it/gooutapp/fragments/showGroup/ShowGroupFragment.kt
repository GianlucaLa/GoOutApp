package it.gooutapp.fragments.showGroup

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
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
import it.gooutapp.MainActivity
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group
import kotlinx.android.synthetic.main.recycle_view_row.view.*

class ShowGroupFragment : Fragment(), MyAdapter.ClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userGroupList: ArrayList<Group>
    private lateinit var adminFlagList: ArrayList<Boolean>
    private lateinit var myAdapter: MyAdapter
    private var user_email = Firebase.auth.currentUser?.email.toString()
    private val fs = FireStore()
    private val TAG = "SHOW_GROUP_FRAGMENT"
    private val OFFSET_PX = 30
    private lateinit var root: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_show_group, container, false)
        return root
    }

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "OOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO")
        var createGroupButton: FloatingActionButton = root.findViewById(R.id.fab)
        recyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)
        userGroupList = arrayListOf()
        adminFlagList = arrayListOf()

        fs.getUserGroupData(user_email) { groupList, adminFlag ->
            userGroupList = groupList
            adminFlagList = adminFlag
            myAdapter = MyAdapter(userGroupList, adminFlagList,this)
            recyclerView.adapter = myAdapter

            //swipe a destra recycle view
            val itemSwipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    Log.e(TAG, viewHolder.adapterPosition.toString())
                    return false
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    //creo background, che in realtà corrisponde allo spazio libero lasciato dalla draw mentre slida a sinistra
                    if(dX > -viewHolder.itemView.width/3){
                        var background = ColorDrawable(Color.GRAY)
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }else{
                        var background = ColorDrawable(ContextCompat.getColor(root.context, R.color.lighRed))
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }
                    val icon = ContextCompat.getDrawable(root.context, R.drawable.ic_menu_trash_sliding)
                    val topMargin = calculateTopMargin(icon!!, viewHolder.itemView).toInt()
                    icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(root.context, R.color.lightGrey), PorterDuff.Mode.SRC_IN)
                    icon?.bounds  = getStartContainerRectangle(viewHolder.itemView, (icon.intrinsicWidth*1.2).toInt(), topMargin, OFFSET_PX, dX)
                    icon?.draw(c)
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    Log.e(TAG, viewHolder.adapterPosition.toString())
                    if(viewHolder.itemView.textViewAdminFlag.text.toString() == "") {
                        var title = resources.getString(R.string.leave_group)
                        var message = resources.getString(R.string.leave_group_message)
                        showDialog(viewHolder, title, message, false)
                    } else {
                        var title = resources.getString(R.string.delete_group)
                        var message = resources.getString(R.string.delete_group_message)
                        showDialog(viewHolder, title, message, true)
                    }
                }
            }
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
                        fs.createGroupData(nomeG, user_email) { result -> Toast.makeText(root.context, R.string.group_creation, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(root.context, R.string.error_empty_value, Toast.LENGTH_SHORT).show()
                    }
                }
                setNegativeButton(R.string.cancel) { dialog, which ->
                    //null operation
                }
                setView(dialogLayout)
                setCancelable(false)
                show()
            }
        }
    }

    //popup per confermare o cancellare row
    private fun showDialog(viewHolder: RecyclerView.ViewHolder, title: String, message: String, delete: Boolean) {
        val builder = view?.let { AlertDialog.Builder(it.context) }
        if (builder != null) {
            builder.setTitle(title)
            builder.setMessage(message)
            Log.e(TAG, viewHolder.adapterPosition.toString())
            builder.setPositiveButton(R.string.ok) {dialog, wich ->
                Log.e(TAG, viewHolder.adapterPosition.toString())
                var position = viewHolder.adapterPosition
                if(delete) {
                    fs.deleteGroupData(userGroupList[position].groupCode.toString()){ result ->
                        if (result) {
                            onResume()
                        } else {
                            Log.e(TAG, "error during delete of document")
                        }
                    }
                }else{
                    fs.leaveGroup(userGroupList[position].groupCode.toString()){ result ->
                        if (result) {
                            onResume()
                        } else {
                            Log.e(TAG, "error during delete of user's field")
                        }
                    }
                }
                userGroupList.removeAt(position)
                myAdapter.notifyItemRemoved(position)
            }
            builder.setNegativeButton(R.string.cancel){dialog, wich ->
                Log.e(TAG, viewHolder.adapterPosition.toString())
                myAdapter.notifyDataSetChanged()
            }
            builder.setCancelable(false);
            builder.show()
        }
    }

    //TODO per creare nuovo layout
    override fun onItemClick(group: Group) {
        //TODO("Not yet implemented")
    }


    private fun getStartContainerRectangle(viewItem: View, iconWidth: Int, topMargin: Int, sideOffset: Int, dx: Float): Rect {
        val leftBound = viewItem.right + dx.toInt() + sideOffset
        val rightBound = viewItem.right + dx.toInt() + iconWidth + sideOffset
        val topBound = viewItem.top + topMargin
        val bottomBound = viewItem.bottom - topMargin

        return Rect(leftBound, topBound, rightBound, bottomBound)
    }

    private fun calculateTopMargin(icon: Drawable, viewItem: View): Float {
        return (viewItem.height - icon.intrinsicHeight) / 2.2F
    }
}