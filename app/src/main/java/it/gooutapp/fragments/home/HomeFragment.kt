package it.gooutapp.fragments.home

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import it.gooutapp.models.Group
import it.gooutapp.models.myDialog
import kotlinx.android.synthetic.main.recycle_view_row.view.*


class HomeFragment : Fragment(), GroupAdapter.ClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userGroupList: ArrayList<Group>
    private lateinit var adminFlagList: ArrayList<Boolean>
    private lateinit var groupAdapter: GroupAdapter
    private var user_email = Firebase.auth.currentUser?.email.toString()
    private val fs = FireStore()
    private val OFFSET_PX = 30
    private lateinit var root: View
    private val TAG = "HOME_FRAGMENT"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        var createGroupButton: FloatingActionButton = root.findViewById(R.id.fab)
        recyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)
        userGroupList = arrayListOf()
        adminFlagList = arrayListOf()

        fs.getUserGroupData(user_email) { groupList, adminFlag ->
            userGroupList = groupList
            adminFlagList = adminFlag
            groupAdapter = GroupAdapter(userGroupList, adminFlagList,this)
            recyclerView.adapter = groupAdapter

            //swipe a destra recycle view
            val itemSwipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
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
            var title = resources.getString(R.string.create_group)
            var message = resources.getString(R.string.enter_group_name)
            myDialog(title, message, root.context, layoutInflater) { groupName ->
                fs.createGroupData(groupName, user_email) { result ->
                    if(result){
                        Toast.makeText(root.context, R.string.group_creation_successful, Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(root.context, R.string.group_creation_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        setHasOptionsMenu(true)
        return root
    }

    //popup per confermare cancellazione row
    private fun showDialog(viewHolder: RecyclerView.ViewHolder, title: String, message: String, delete: Boolean) {
        val builder = view?.let { AlertDialog.Builder(it.context) }
        if (builder != null) {
            builder.setTitle(title)
            builder.setMessage(message)
            builder.setPositiveButton(R.string.ok) {dialog, wich ->
                var position = viewHolder.adapterPosition
                groupAdapter.onDeleteItem(position)
                //se utente amministratore
                if(delete) {
                    fs.deleteGroupData(userGroupList[position].groupCode.toString()){ result ->
                        if (result) {
                            fs.getUserGroupData(user_email) { groupList, adminFlag ->
                                userGroupList = groupList
                                adminFlagList = adminFlag
                                groupAdapter = GroupAdapter(userGroupList, adminFlagList, this)
                                recyclerView.adapter = groupAdapter
                            }
                        } else {
                            Log.e(TAG, "error during delete of document")
                        }
                    }
                //se utente non amministratore
                }else{
                    fs.leaveGroup(userGroupList[position].groupCode.toString()){ result ->
                        if (result) {
                            fs.getUserGroupData(user_email) { groupList, adminFlag ->
                                userGroupList = groupList
                                adminFlagList = adminFlag
                                groupAdapter = GroupAdapter(userGroupList, adminFlagList, this)
                                recyclerView.adapter = groupAdapter
                            }
                        } else {
                            Log.e(TAG, "error during delete of user's field")
                        }
                    }
                }
                userGroupList.removeAt(position)
                groupAdapter.notifyItemRemoved(position)
            }
            builder.setNegativeButton(R.string.cancel){dialog, wich ->
                groupAdapter.notifyDataSetChanged()
            }
            builder.setCancelable(false);
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onItemClick(group: Group) {
        val bundle = bundleOf(
            "groupName" to group.groupName,
            "groupCode" to group.groupCode
        )
        activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_home_to_nav_group, bundle)
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