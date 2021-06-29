package it.gooutapp.fragment

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.adapter.GroupAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Group
import it.gooutapp.model.MyDialog
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.group_row.view.*

class HomeFragment : Fragment(), GroupAdapter.ClickListener {
    private val TAG = "HOME_FRAGMENT"
    private lateinit var recyclerView: RecyclerView
    private lateinit var userGroupList: ArrayList<Group>
    private lateinit var adminFlagList: ArrayList<Boolean>
    private lateinit var groupAdapter: GroupAdapter
    private var user_email = Firebase.auth.currentUser?.email.toString()
    private val fs = FireStore()
    private val OFFSET_PX = 30
    private lateinit var root: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        recyclerView = root.recycleView
        recyclerView.layoutManager = LinearLayoutManager(root.context)
        recyclerView.setHasFixedSize(true)
        userGroupList = arrayListOf()
        adminFlagList = arrayListOf()

        fs.getUserGroupData { groupList, adminFlag ->
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
                        val background = ColorDrawable(Color.GRAY)
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }else{
                        val background = ColorDrawable(ContextCompat.getColor(root.context, R.color.lighRed))
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }
                    val icon = ContextCompat.getDrawable(root.context, R.drawable.ic_menu_trash_sliding)
                    val topMargin = calculateTopMargin(icon!!, viewHolder.itemView).toInt()
                    icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(root.context, R.color.lightGrey), PorterDuff.Mode.SRC_IN)
                    icon?.bounds  = getStartContainerRectangle(viewHolder.itemView, (icon.intrinsicWidth*1.2).toInt(), topMargin, OFFSET_PX, dX)
                    icon?.draw(c)
                }

                @RequiresApi(Build.VERSION_CODES.O)
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val userIsAdmin = viewHolder.itemView.textViewAdminFlag.text.toString() != ""
                    var title = if(userIsAdmin) resources.getString(R.string.delete_group) else resources.getString(R.string.leave_group)
                    var message = if(userIsAdmin) resources.getString(R.string.delete_group_message) else resources.getString(R.string.leave_group_message)
                    MyDialog(title, message, root.context) { confirm ->
                        if (confirm) {
                            if (userIsAdmin) {
                                fs.deleteGroupData(userGroupList[position].groupId.toString()) { result ->
                                    groupAdapter.deleteItemRow(position)
                                    if (!result) Log.e(TAG, "error during delete of document")
                                }
                            } else {
                                fs.leaveGroup(userGroupList[position].groupId.toString()) { result ->
                                    groupAdapter.deleteItemRow(position)
                                    if (!result) Log.e(TAG, "error during update of document")
                                }
                            }
                            userGroupList.removeAt(position)
                            groupAdapter.notifyItemRemoved(position)
                        } else {
                            groupAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            val swap = ItemTouchHelper(itemSwipe)
            swap.attachToRecyclerView(recyclerView)
        }

        //Create Group Listener
        root.newGroupFab.setOnClickListener { view ->
            var title = resources.getString(R.string.create_group)
            var message = resources.getString(R.string.enter_group_name)
            MyDialog(title, message, root.context, layoutInflater) { groupName ->
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onItemClick(group: Group) {
        val bundle = bundleOf(
            "groupName" to group.groupName,
            "groupId" to group.groupId
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