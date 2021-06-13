package it.gooutapp.models

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import it.gooutapp.R

class myDialog {
    constructor(title: String, message: String, context: Context, inflater: LayoutInflater, callback: (String) -> Unit){
        val builder = AlertDialog.Builder(context)
        val dialogLayout = inflater.inflate(R.layout.edittext_my_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextMyDialog)
        with(builder) {
            setTitle(title)
            editText.hint = message
            setPositiveButton(R.string.ok) { dialog, which ->
                //rimuovo eventuali spazi vuoti inseriti dall'utente
                val input = editText.text.toString().replace("\\s+".toRegex(), "")
                if(input != ""){
                    callback(input)
                }else{
                    android.widget.Toast.makeText(context, R.string.error_empty_value, android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            setNegativeButton(R.string.cancel) { dialog, which ->
                //do nothing
            }
            setView(dialogLayout)
            setCancelable(false)
            show()
        }
    }
}