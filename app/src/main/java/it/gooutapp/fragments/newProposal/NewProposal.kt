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
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import java.util.*

class NewProposal : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    lateinit var c: Calendar
    lateinit var d: Calendar
    lateinit var dateView: TextView
    lateinit var timeView: TextView
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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_new_proposal, container, false)
        pickDate()
        pickTime()
        return root
    }

    private fun getDateCalendar() {
        c = Calendar.getInstance()
        day = c.get(Calendar.DAY_OF_MONTH)
        month = c.get(Calendar.MONTH)
        year = c.get(Calendar.YEAR)
    }

    private fun pickDate() {
        dateView = root.findViewById(R.id.editTextDatePicker)
        dateView.setOnClickListener {
            getDateCalendar()
            DatePickerDialog( dateView.context, this , year,  month, day).show()
        }
    }

    private fun getTimeCalendar() {
        d = Calendar.getInstance()
        hour = d.get(Calendar.HOUR)
        minute = d.get(Calendar.MINUTE)
    }

    private fun pickTime() {
        timeView = root.findViewById(R.id.editTextHourPicker)
        timeView.setOnClickListener {
            getTimeCalendar()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        mDay = dayOfMonth
        mMonth = month
        mYear = year
        getDateCalendar()
        var date  = "$mDay-$mMonth-$mYear"
        editTextDatePicker.setText(date)
        TimePickerDialog(dateView.context, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        mHour = hourOfDay
        mMinute = minute
        getTimeCalendar()
        var time  = "Hour: $mHour Minute: $mMinute"
        editTextHourPicker.setText(time)
        Toast.makeText(root.context, "$time", Toast.LENGTH_SHORT).show()
    }

}