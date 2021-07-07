package it.gooutapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import it.gooutapp.R
import it.gooutapp.adapter.MemberAdapter

class MemberFragment: Fragment(), MemberAdapter.ClickListenerMember {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_member, container, false)

        return root
    }
}