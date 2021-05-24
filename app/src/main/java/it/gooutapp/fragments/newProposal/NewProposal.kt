package it.gooutapp.fragments.newProposal

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.DatePicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import it.gooutapp.R
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import kotlinx.android.synthetic.main.fragment_new_proposal.view.*
import java.time.DayOfWeek
import java.time.Month
import java.time.Year
import java.util.*

class NewProposal : Fragment() {
    //lateinit var calendar: CalendarView
    lateinit var c : Calendar

    lateinit var btn : Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_new_proposal, container, false)
        btn = root.findViewById<Button>(R.id.buttonPickDate)

        c = Calendar.getInstance()
        var day = c.get(Calendar.DAY_OF_MONTH)
        var month = c.get(Calendar.MONTH)
        var year = c.get(Calendar.YEAR)

        btn.setOnClickListener {
            val dpd = DatePickerDialog(btn.context, DatePickerDialog.OnDateSetListener{ datePicker: DatePicker, mDay: Int, mMonth: Int, mYear: Int ->
                Toast.makeText(btn.context, ""+ mDay + "/" + mMonth + "/" + mYear, Toast.LENGTH_SHORT).show()
        }, day, month, year )
            dpd.show()
        }

        /*calendar = root.findViewById(R.id.calendarView)

        calendar.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            val msg = "Selected date is " + dayOfMonth + "/" + (month + 1) + "/" + year
            Toast.makeText(calendar.context, msg, Toast.LENGTH_SHORT).show()
        }*/


        return root
    }
}