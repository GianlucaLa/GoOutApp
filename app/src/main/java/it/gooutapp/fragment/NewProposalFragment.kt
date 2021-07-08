package it.gooutapp.fragment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.transition.MaterialFade
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.fragment_new_proposal.*
import kotlinx.android.synthetic.main.fragment_new_proposal.view.*
import java.time.LocalDateTime
import java.util.*

class NewProposalFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var groupId: String
    private lateinit var groupName: String
    private lateinit var proposalId: String
    private var placeString = ""
    private val fs = FireStore()
    private lateinit var c: Calendar
    private lateinit var proposalNameEditText: EditText
    private lateinit var placePickerEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var confirmProposalButton: Button
    private lateinit var root: View
    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var time = ""
    private var date = ""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_new_proposal, container, false)

        //se si apre per modificare la proposal
        if(arguments?.get("modify") == true){
            proposalId = arguments?.get("proposalId").toString()
            setProposalText()
        }else {
            groupId = arguments?.getString("groupId").toString()
            groupName = arguments?.get("groupName").toString()
        }
        proposalNameEditText = root.editTextNameProposal
        proposalNameEditText.addTextChangedListener {
            if(proposalNameEditText.text.length == 15){ Toast.makeText(root.context, R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextNameProposalView.isErrorEnabled = false
        }
        placePickerEditText = root.editTextPlace
        placePickerEditText.addTextChangedListener {
            editTextPlaceView.isErrorEnabled = false
        }
        placePickerEditText.setOnClickListener {
            startAutocompleteActivity()
        }
        dateEditText = root.editTextDate
        dateEditText.addTextChangedListener {
            editTextDateView.isErrorEnabled = false
        }
        timeEditText = root.editTextHour
        timeEditText.addTextChangedListener {
            editTextHourView.isErrorEnabled = false
        }
        confirmProposalButton = root.confirmProposal
        confirmProposalButton.setOnClickListener{proposalConfirm()}

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
        c = Calendar.getInstance()
        hour = c.get(Calendar.HOUR_OF_DAY)
        minute = c.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        dateEditText.setOnClickListener {
            editTextDate.isEnabled = false;
            getDateCalendar()
            var dateDialog = DatePickerDialog( dateEditText.context, this , year,  month, day)
            dateDialog.setOnCancelListener {
                editTextDate.isEnabled = true;
            }
            dateDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            dateDialog.show()
        }
    }

    private fun pickTime() {
        timeEditText.setOnClickListener {
            if (editTextDate.text.toString() != "") {
                editTextHour.isEnabled = false;
                getTimeCalendar()
                var timeDialog = TimePickerDialog(dateEditText.context, this, hour, minute, true)
                timeDialog.setOnCancelListener {
                    editTextHour.isEnabled = true;
                }
                val materialFade = MaterialFade().apply {
                    duration = 150L
                }
                timeDialog.show()
            } else {
                Toast.makeText(root.context, R.string.compile_date_first, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        date = if(month+1<10) "$year-0${month+1}" else "$year-${month+1}"
        date += if(dayOfMonth<10) "-0$dayOfMonth" else "-$dayOfMonth"
        getDateCalendar()
        editTextDate.setText(date)
        timeEditText.setText("")
        editTextDate.isEnabled = true;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        getTimeCalendar()
        time = if(hourOfDay<10) "0$hourOfDay" else "$hourOfDay"
        time += if(minute<10) ":0$minute" else ":$minute"
        var date = LocalDateTime.parse("$date"+"T$time")
        if(date.isAfter(LocalDateTime.now())){
            editTextHour.setText(time)
            editTextHour.isEnabled = true;
        } else {
            Toast.makeText(root.context, R.string.time_not_valid, Toast.LENGTH_SHORT).show()
            editTextHour.isEnabled = true;
        }
    }

    private fun startAutocompleteActivity() {
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
            Log.i("MAPS", "Place: ${place?.name}, ${place?.id}")
        } else if(resultCode == AutocompleteActivity.RESULT_ERROR) {
            var status = data?.let { Autocomplete.getStatusFromIntent(it) }
            Log.i("MAPS", "An error occurred: $status")
        }
        placePickerEditText.isEnabled = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun proposalConfirm(){
        var proposalName = proposalNameEditText.text.toString()
        if(proposalName.isEmpty()) editTextNameProposalView.error = resources.getString(R.string.name_empty_error)
        if(editTextPlace.text.toString() == "") editTextPlaceView.error = resources.getString(R.string.place_empty_error)
        if(editTextDate.text.toString() == "") editTextDateView.error = resources.getString(R.string.date_empty_error)
        if(editTextHour.text.toString() == "") editTextHourView.error = resources.getString(R.string.time_empty_error)
        if(!(editTextNameProposalView.isErrorEnabled || editTextPlaceView.isErrorEnabled || editTextDateView.isErrorEnabled || editTextHourView.isErrorEnabled)) {
            val dateTime = "$date"+"T$time"
            if(arguments?.get("place") != null){
                fs.modifyProposalData(proposalId, proposalName, dateTime, placeString, arguments?.get("groupId").toString(), arguments?.get("organizator").toString(), arguments?.get("organizatorId").toString(), arguments?.get("groupName").toString()) { result ->
                    if (result) {
                        activity?.findNavController(R.id.nav_host_fragment)?.navigateUp()
                        Toast.makeText(root.context, R.string.successfulProposalModified, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(root.context, R.string.failProposalModify, Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                fs.createProposalData(groupId, proposalName, dateTime, placeString, groupName) { result ->
                    if (result) {
                        activity?.findNavController(R.id.nav_host_fragment)?.navigateUp()
                        Toast.makeText(root.context, R.string.successfulProposalCreation, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(root.context, R.string.failProposalCreation, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setProposalText(){
        root.editTextNameProposal.setText(arguments?.get("proposalName").toString())
        placeString = arguments?.get("place").toString()
        root.editTextPlace.setText(placeString)
    }
}