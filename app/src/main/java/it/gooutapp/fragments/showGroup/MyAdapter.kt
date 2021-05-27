package it.gooutapp.fragments.showGroup

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.google.firebase.firestore.DocumentSnapshot
import it.gooutapp.R


class myAdapter(private val context: Context, private val data: DocumentSnapshot) : BaseAdapter() {

    override fun getCount(): Int {
        TODO()
    }

    override fun getItem(position: Int): Any {
     TODO()
    }

    override fun getItemId(position: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
      var newView = convertView
        if(convertView == null)
            newView = LayoutInflater.from(context).inflate(R.layout.recycle_view_row, parent, false)
            if(newView != null) {
                val nomeGruppo : TextView = newView.findViewById(R.id.textViewNomeGruppo)
                val numPartecipanti : TextView = newView.findViewById(R.id.textViewNumPartecipanti)
                val posizione : TextView = newView.findViewById(R.id.textViewNumGruppo)
            }
        return newView
    }
}

