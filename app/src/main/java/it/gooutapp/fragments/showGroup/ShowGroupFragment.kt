package it.gooutapp.fragments.showGroup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.gooutapp.R

class ShowGroupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_show_group, container, false)
        val plusButton: FloatingActionButton = root.findViewById(R.id.fab)

        plusButton.setOnClickListener { view ->
            val builder = AlertDialog.Builder(view.context)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.popup_layout, null)
            val editText = dialogLayout.findViewById<EditText>(R.id.editTextPopup)

            with(builder) {
                setTitle(R.string.join_group)
                setPositiveButton(R.string.ok) { dialog, which ->
                    val testo = editText.text.toString()
                }
                setNegativeButton(R.string.cancel) { dialog, which ->
                    //null operation
                }
                setView(dialogLayout)
                show()
            }
        }
        return root
    }

}