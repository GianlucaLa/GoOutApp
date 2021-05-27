package it.gooutapp.fragments.newProposal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import it.gooutapp.R
import java.util.*

class NewProposal : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    lateinit var c: Calendar
    lateinit var btn: Button
    lateinit var root: View
    var day = 0
    var month = 0
    var year = 0
    var hour = 0
    var minute = 0

    var mDay = 0
    var mMonth = 0
    var mYear = 0
    var mHour = 0
    var mMinute = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_new_proposal, container, false)
        pickDate()
        return root
    }

    private fun getDateTimeCalendar() {
        c = Calendar.getInstance()
        day = c.get(Calendar.DAY_OF_MONTH)
        month = c.get(Calendar.MONTH)
        year = c.get(Calendar.YEAR)
        hour = c.get(Calendar.HOUR)
        minute = c.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        btn = root.findViewById<Button>(R.id.buttonPickDate)

        btn.setOnClickListener {
            getDateTimeCalendar()

            DatePickerDialog( btn.context, this , year,  month, day).show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mDay = dayOfMonth
        mMonth = month
        mYear = year

        getDateTimeCalendar()

        TimePickerDialog(btn.context, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mHour = hourOfDay
        mMinute = minute

        val msg = "$mDay-$mMonth-$mYear--- Hour: $mHour Minute: $mMinute"
        Toast.makeText(root.context, "$msg", Toast.LENGTH_SHORT).show()
    }
}