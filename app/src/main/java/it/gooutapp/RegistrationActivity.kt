package it.gooutapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.registration.*

class RegistrationActivity: AppCompatActivity() {
    val fs : FireStore = FireStore()
    private val TAG = "REGISTRATION_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        editTextName.addTextChangedListener {
            if (editTextName.text?.length == 15) { Toast.makeText(this, R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextNameView.isErrorEnabled = false
        }
        editTextSurname.addTextChangedListener {
            if (editTextSurname.text?.length == 15) { Toast.makeText(this, R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextSurnameView.isErrorEnabled = false
        }
        editTextNickname.addTextChangedListener {
            if (editTextNickname.text?.length == 15) { Toast.makeText(this, R.string.max15chars, Toast.LENGTH_SHORT).show() }
            editTextNicknameView.isErrorEnabled = false
        }
        editTextEmail.addTextChangedListener{
            editTextEmailView.isErrorEnabled = false
        }
        editTextPassword.addTextChangedListener{
            if (editTextPassword.text?.length == 15) { Toast.makeText(this, R.string.max20characters, Toast.LENGTH_SHORT).show() }
            editTextPasswordView.isErrorEnabled = false
        }
    }

    fun closeActivity() {
        var resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    fun createUser(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(applicationContext, getString(R.string.successful_registration), Toast.LENGTH_SHORT).show()
                    closeActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun fieldsCheck(view: View){
        val name = editTextName.text.toString()
        val surname = editTextSurname.text.toString()
        val nickname = editTextNickname.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Check for a valid email address.
        when {
            email.isEmpty() -> { editTextEmailView.error = resources.getString(R.string.email_empty_error) }
            email.length < 6 -> { editTextEmailView.error = resources.getString(R.string.error_invalid_email) }
        }

        // Check for a valid password.
        when {
            password!!.isEmpty() -> { editTextPasswordView.error = resources.getString(R.string.password_empty_error) }
            password.length < 6 -> { editTextPasswordView.error = resources.getString(R.string.error_invalid_password) }
        }

        //check others
        if(name.isEmpty()) editTextNameView.error = resources.getString(R.string.name_empty_error)
        if(surname.isEmpty()) editTextSurnameView.error = resources.getString(R.string.surname_empty_error)
        if(nickname.isEmpty())editTextNicknameView.error = resources.getString(R.string.nickname_empty_error)

        //final check
        if (!(editTextNameView.isErrorEnabled || editTextSurnameView.isErrorEnabled || editTextNicknameView.isErrorEnabled || editTextEmailView.isErrorEnabled || editTextPasswordView.isErrorEnabled)) {
            fs.createUserData(name, surname, nickname, email)
            createUser(email, password)
        }
    }
}