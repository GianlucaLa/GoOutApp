package it.gooutapp.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.regex.Pattern

class RegistrationActivity: AppCompatActivity() {
    private val TAG = "REGISTRATION_ACTIVITY"
    private val fs: FireStore = FireStore()
    private var userAuthCreated: Boolean = false
    private var userFirestoreCreated: Boolean = false
    private val PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[@!?#$%^&+=])" + "(?=.*?[A-Z])" + "(?=\\S+$)" + ".{6,}" + "$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseFirestore.getInstance().enableNetwork()
        setContentView(R.layout.activity_registration)
        editTextName.addTextChangedListener {
            if (editTextName.text?.length == 15) { Toast.makeText(this, R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextNameView.isErrorEnabled = false
        }
        editTextSurname.addTextChangedListener {
            if (editTextSurname.text?.length == 15) { Toast.makeText(this,
                R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextSurnameView.isErrorEnabled = false
        }
        editTextNickname.addTextChangedListener {
            if (editTextNickname.text?.length == 15) { Toast.makeText(this,
                R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextNicknameView.isErrorEnabled = false
        }
        editTextEmail.addTextChangedListener{
            editTextEmailView.isErrorEnabled = false
        }
        editTextPassword.addTextChangedListener{
            if (editTextPassword.text?.length == 15) { Toast.makeText(this,
                R.string.max20characters, Toast.LENGTH_SHORT).show() }
            editTextPasswordView.isErrorEnabled = false
        }
    }

    private fun closeActivity() {
        var resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun checkDuplicateNickname(name: String, surname: String, nickname: String, email: String){
        fs.checkDuplicateNickname(nickname) { isDuplicate ->
            if(isDuplicate){
                Toast.makeText(applicationContext, getString(R.string.duplicate_nickname), Toast.LENGTH_SHORT).show()
                Rpb?.visibility = View.INVISIBLE
            }else {
                fs.createUserData(name, surname, nickname, email){ result ->
                    if(result){
                        userFirestoreCreated = true
                        Log.d(TAG, "createUserWithEmail:success")
                        Toast.makeText(applicationContext, getString(R.string.successful_registration), Toast.LENGTH_SHORT).show()
                        closeActivity()
                    }else{
                        Toast.makeText(applicationContext, getString(R.string.error), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }


    private fun createUser(email: String, password: String, name: String, surname: String, nickname: String) {
        if (userAuthCreated){
            checkDuplicateNickname(name, surname, nickname, email)
        }else{
            Firebase.auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        userAuthCreated = true
                        checkDuplicateNickname(name, surname, nickname, email)
                    } else {
                        // If sign in fails, display a message to the user.
                        Rpb?.visibility = View.INVISIBLE
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    }
                }
                .addOnFailureListener { error ->
                    if(error.message.toString() == "The email address is already in use by another account.")
                        Toast.makeText(applicationContext, resources.getString(R.string.duplicate_mail), Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    private fun CharSequence?.isValidPassword() = !isNullOrEmpty() && PASSWORD_PATTERN.matcher(this).matches()

    fun fieldsCheck(view: View){
        Rpb?.visibility = View.VISIBLE
        val name = editTextName.text.toString()
        val surname = editTextSurname.text.toString()
        val nickname = editTextNickname.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Check for a valid email address.
        when {
            !email.isValidEmail() -> { editTextEmailView.error = resources.getString(R.string.error_invalid_email) }
        }

        // Check for a valid password.
        when {
            !password.isValidPassword() -> { editTextPasswordView.error = resources.getString(R.string.error_invalid_password) }
        }

        //check others
        if(name.isEmpty()) editTextNameView.error = resources.getString(R.string.name_empty_error)
        if(surname.isEmpty()) editTextSurnameView.error = resources.getString(R.string.surname_empty_error)
        if(nickname.isEmpty())editTextNicknameView.error = resources.getString(R.string.nickname_empty_error)

        //final check
        if (!(editTextNameView.isErrorEnabled || editTextSurnameView.isErrorEnabled || editTextNicknameView.isErrorEnabled || editTextEmailView.isErrorEnabled || editTextPasswordView.isErrorEnabled)) {
            createUser(email, password, name, surname, nickname)
        }else{
            Rpb?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!userFirestoreCreated) {
            Firebase.auth.currentUser?.delete()
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User account deleted.")
                    }
                }
        }
    }
}