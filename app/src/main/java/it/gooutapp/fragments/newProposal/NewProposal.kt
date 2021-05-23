package it.gooutapp.fragments.newProposal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment
import it.gooutapp.R

class NewProposal : Fragment() {
    lateinit var calendar: CalendarView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_new_proposal, container, false)
        calendar = root.findViewById(R.id.calendarView)

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            val msg = "Selected date is " + dayOfMonth + "/" + (month + 1) + "/" + year
            Toast.makeText(calendar.context, msg, Toast.LENGTH_SHORT).show()
        }

        return root
    }



    fun confirmDate(view: View){

    }
}