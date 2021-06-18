package it.gooutapp.fragments.group

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import it.gooutapp.R
import it.gooutapp.models.Group

class GroupFragment : Fragment() {
    private val TAG = "GROUP_FRAGMENT"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_group, container, false)
        setHasOptionsMenu(true)
        return root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.group_fragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }
}