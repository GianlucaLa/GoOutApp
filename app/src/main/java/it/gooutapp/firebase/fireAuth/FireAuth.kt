package it.gooutapp.firebase.fireAuth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FireAuth {
    private var db = FirebaseFirestore.getInstance()
    private val TAG = "FIRE_AUTH"
    private lateinit var auth: FirebaseAuth

}