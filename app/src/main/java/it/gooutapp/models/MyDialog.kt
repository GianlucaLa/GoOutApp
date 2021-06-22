package it.gooutapp.models

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import it.gooutapp.R

class myDialog {
    constructor(title: String, message: String, context: Context, inflater: LayoutInflater, callback: (String) -> Unit){
        val builder = AlertDialog.Builder(context)
        val dialogLayout = inflater.inflate(R.layout.edittext_my_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextMyDialog)
        with(builder) {
            setTitle(title)
            editText.addTextChangedListener {
                if(editText.text.length == 15){
                    Toast.makeText(context, R.string.max15chars, Toast.LENGTH_SHORT).show()
                }
            }
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