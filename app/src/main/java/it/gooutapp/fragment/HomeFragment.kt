package it.gooutapp.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.activity.MainActivity
import it.gooutapp.adapter.GroupAdapter
import it.gooutapp.firebase.FireStore
import it.gooutapp.model.Group
import it.gooutapp.model.MyDialog
import kotlinx.android.synthetic.main.fragment_group.*
import kotlinx.android.synthetic.main.fragment_history.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.row_group.view.*

class HomeFragment : Fragment(), GroupAdapter.ClickListener {
    private val TAG = "HOME_FRAGMENT"
    private var curr_user_email = Firebase.auth.currentUser?.email.toString()
    private lateinit var recyclerView: RecyclerView
    private lateinit var groupAdapter: GroupAdapter
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private var mLastClickTime: Long = 0
    private val fs = FireStore()
    private val OFFSET_PX = 30
    private lateinit var root: View

    @SuppressLint("NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_home, container, false)
        val linearLayoutManager = LinearLayoutManager(root.context)
        recyclerView = root.recyclerView
        recyclerView.layoutManager = linearLayoutManager
        swipeRefreshLayout = root.findViewById(R.id.swipeRefreshLayoutHome)
        swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.colorPrimary))
        loadRecyclerData(root)

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.isRefreshing = false
            loadRecyclerData(root)
        }

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && newGroupFab.isShown) newGroupFab.hide()
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) newGroupFab.show()
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        //Create Group Listener
        root.newGroupFab.setOnClickListener {
            var title = resources.getString(R.string.create_group)
            var message = resources.getString(R.string.enter_group_name)
            MyDialog(title, message, root.context, layoutInflater, true) { groupName ->
                fs.createGroupData(groupName, curr_user_email) { result ->
                    if(result){
                        val activity: Activity? = activity
                        if (activity != null && activity is MainActivity) {
                            val mainActivity: MainActivity = activity
                            mainActivity.refreshHome()
                            groupAdapter.notifyDataSetChanged()
                        }
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadRecyclerData(root: View){
        fs.getUserHomeData(root.context){ groupList, adminFlag, notificationHM, lastMessageHM ->
            val emptyHomeMessage = root.tvEmptyGroupMessage
            emptyHomeMessage.text = context?.resources?.getString(R.string.empty_home_message)
            groupAdapter = GroupAdapter(groupList, adminFlag, notificationHM, lastMessageHM,this, emptyHomeMessage)
            recyclerView.adapter = groupAdapter
            HomePB?.visibility = View.INVISIBLE
            if(groupList.size == 0){
                tvEmptyGroupMessage?.visibility = View.VISIBLE
            }

            //swipe a destra recycle view
            val itemSwipe = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    return false
                }

                override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    //creo background, che in realt?? corrisponde allo spazio libero lasciato dalla draw mentre slida a sinistra
                    if(dX > -viewHolder.itemView.width/3){
                        val background = ColorDrawable(Color.GRAY)
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }else{
                        val background = ColorDrawable(ContextCompat.getColor(root.context, R.color.lightRed))
                        background.setBounds((viewHolder.itemView.right + dX).toInt(), viewHolder.itemView.top, viewHolder.itemView.right, viewHolder.itemView.bottom)
                        background.draw(c)
                    }
                    val icon = ContextCompat.getDrawable(root.context, R.drawable.ic_menu_trash_sliding)
                    val topMargin = calculateTopMargin(icon!!, viewHolder.itemView).toInt()
                    icon.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(root.context, R.color.trashGrey), PorterDuff.Mode.SRC_IN)
                    icon?.bounds  = getStartContainerRectangle(viewHolder.itemView, (icon.intrinsicWidth*1.2).toInt(), topMargin, OFFSET_PX, dX)
                    icon?.draw(c)
                }

                @SuppressLint("NotifyDataSetChanged")
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val position = viewHolder.adapterPosition
                    val userIsAdmin = viewHolder.itemView.textViewAdminFlag.text.toString() != ""
                    var title = if(userIsAdmin) resources.getString(R.string.delete_group) else resources.getString(R.string.leave_group)
                    var message = if(userIsAdmin) resources.getString(R.string.delete_group_message) else resources.getString(R.string.leave_group_message)
                    MyDialog(title, message, root.context) { confirm ->
                        if (confirm) {
                            var thisPosition = viewHolder.adapterPosition
                            groupAdapter.deleteItemRow(thisPosition)
                            if (userIsAdmin) {
                                fs.deleteGroupData(groupList[thisPosition].groupId.toString())
                            } else {
                                fs.leaveGroup(groupList[thisPosition].groupId.toString()) { result ->
                                    if (!result) Log.e(TAG, "error during update of document")
                                }
                            }
                            groupList.removeAt(position)
                            groupAdapter.notifyDataSetChanged()
                            if(groupList.size == 0)
                                tvEmptyGroupMessage.visibility = View.VISIBLE
                        }else{
                            groupAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
            val swap = ItemTouchHelper(itemSwipe)
            swap.attachToRecyclerView(recyclerView)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.home_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onItemClick(group: Group) {
        fs.getUserData(curr_user_email) getGroupPopupNotification@{ userData ->
            val bundle = bundleOf(
                "groupName" to group.groupName,
                "groupId" to group.groupId,
                "userData" to userData
            )
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
                return@getGroupPopupNotification;
            }
            mLastClickTime = SystemClock.elapsedRealtime()
            activity?.findNavController(R.id.nav_host_fragment)?.navigate(R.id.action_nav_home_to_nav_group, bundle)
        }
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