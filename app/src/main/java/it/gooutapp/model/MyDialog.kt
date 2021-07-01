package it.gooutapp.model

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import it.gooutapp.R

class MyDialog {
    constructor(title: String, message: String, context: Context, inflater: LayoutInflater, callback: (String) -> Unit){
        val builder = AlertDialog.Builder(context)
        val dialogLayout = inflater.inflate(R.layout.edittext_my_dialog, null)
        val editText = dialogLayout.findViewById<EditText>(R.id.editTextMyDialog)
        with(builder) {
            setTitle(title)
            editText.addTextChangedListener {
                if(editText.text.length > 15){
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
                    Toast.makeText(context, R.string.error_empty_value, android.widget.Toast.LENGTH_SHORT).show()
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

    constructor(title: String, message: String, context: Context, callback: (Boolean) -> Unit) {
        val builder = AlertDialog.Builder(context)
        with(builder) {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.ok) { dialog, wich ->
                callback(true)
            }
            setNegativeButton(R.string.cancel) { dialog, wich ->
                callback(false)
            }
            setCancelable(false);
            show()
        }
    }

    constructor(title: String, message: String, context: Context) {
        val builder = AlertDialog.Builder(context)
        with(builder) {
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.copy) { dialog, which ->
                val myClipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val myClip: ClipData = ClipData.newPlainText("Label", message)
                myClipboard.setPrimaryClip(myClip)
                Toast.makeText(context, R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT).show();
            }
            setCancelable(false);
            show()
        }
    }
}