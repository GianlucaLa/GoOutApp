package it.gooutapp.fragments.newProposal

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import java.util.*

class NewProposal : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var groupCode: String
    private var placeString = ""
    private val fs = FireStore()
    private lateinit var c: Calendar
    private lateinit var d: Calendar
    private lateinit var proposalNameEditText: EditText
    private lateinit var placePickerEditText: EditText
    private lateinit var dateView: EditText
    private lateinit var timeView: EditText
    private lateinit var confirmProposalButton: Button
    private lateinit var root: View
    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    private var time = ""
    private var date = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_new_proposal, container, false)
        confirmProposalButton = root.findViewById(R.id.confirmProposal)
        proposalNameEditText = root.findViewById(R.id.editTextNameProposal)
        confirmProposalButton.setOnClickListener{proposalConfirm()}
        placePickerEditText = root.findViewById(R.id.editTextPlace)
        placePickerEditText.setOnClickListener(){
            startAutocompleteActivity()
        }
        groupCode = arguments?.getString("groupCode") as String
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

    private fun getTimeCalendar() {
        d = Calendar.getInstance()
        hour = d.get(Calendar.HOUR_OF_DAY)
        minute = d.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        dateView = root.findViewById(R.id.editTextDate)
        dateView.setOnClickListener {
            editTextDate.isEnabled = false;
            getDateCalendar()
            var dateDialog = DatePickerDialog( dateView.context, this , year,  month, day)
            dateDialog.setOnCancelListener(){
                editTextDate.isEnabled = true;
            }
            dateDialog.show()
        }
    }

    private fun pickTime() {
        timeView = root.findViewById(R.id.editTextHour)
        timeView.setOnClickListener {
            editTextHour.isEnabled = false;
            getTimeCalendar()
            var timeDialog = TimePickerDialog(dateView.context, this, hour, minute, true)
            timeDialog.setOnCancelListener(){
                editTextHour.isEnabled = true;
            }
            timeDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = if(month<10) "$year/0$month" else "$month"
        date += if(dayOfMonth<10) "/0$dayOfMonth" else "/$dayOfMonth"
        getDateCalendar()
        editTextDate.setText(date)
        editTextDate.isEnabled = true;
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        time = if(hourOfDay<10) "0$hourOfDay" else "$hourOfDay"
        time += if(minute<10) ":0$minute" else ":$minute"
        getTimeCalendar()
        editTextHour.setText(time)
        editTextHour.isEnabled = true;
    }

    fun startAutocompleteActivity() {
        placePickerEditText.isEnabled = false
        Places.initialize(root.context, resources.getString(R.string.places_api_key))
        val intent = Autocomplete.IntentBuilder(
            AutocompleteActivityMode.OVERLAY,
            listOf(Place.Field.ID, Place.Field.NAME)
        ).setTypeFilter(TypeFilter.ESTABLISHMENT).build(root.context)
        startActivityForResult(intent, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == AppCompatActivity.RESULT_OK) {
            var place = data?.let { Autocomplete.getPlaceFromIntent(it) }
            placePickerEditText.setText(place?.name)
            placeString = place?.name.toString()
            Toast.makeText(root.context, "Place: ${place?.name}, ${place?.id}", Toast.LENGTH_SHORT).show()
            Log.i("MAPS", "Place: ${place?.name}, ${place?.id}")
        } else if(resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status = data?.let { Autocomplete.getStatusFromIntent(it) }
            Log.i("MAPS", "An error occurred: $status")
        }
        placePickerEditText.isEnabled = true
    }

    //TODO mettere controllo sugli input
    fun proposalConfirm(){
        var proposalName = proposalNameEditText.text.toString()
        //var date = Date(mDay, mMonth, mYear, mHour, mMinute)
        fs.createProposalData(groupCode, proposalName, date, time, placeString){ result ->
            if(result){
                activity?.findNavController(R.id.nav_host_fragment)?.navigateUp()
                Toast.makeText(root.context, R.string.successfulProposal, Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(root.context, R.string.failProposal, Toast.LENGTH_SHORT).show()
            }
        }
    }

}