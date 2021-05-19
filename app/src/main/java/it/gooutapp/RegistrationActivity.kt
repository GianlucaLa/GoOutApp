package it.gooutapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.firestore.FireStore
import kotlinx.android.synthetic.main.registration.*


class RegistrationActivity: AppCompatActivity() {
    val fs :FireStore = FireStore()
    private lateinit var auth: FirebaseAuth
    private val TAG = "REGISTRATION_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        auth = Firebase.auth
    }

    fun closeActivity() {
        var resultIntent = Intent()
        val nickname = editTextNickname.text.toString()
        val password = editTextPassword.text.toString()
        resultIntent.putExtra("nick", nickname)
        resultIntent.putExtra("pwd", password)
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

     fun checkRegistration(v: View)  {
        if(fieldsCheck()) {
            Toast.makeText(applicationContext, getString(R.string.successful_registration), Toast.LENGTH_SHORT).show()
            closeActivity()
        } else {
            Toast.makeText(applicationContext, getString(R.string.registerCheckMassage), Toast.LENGTH_SHORT).show()
        }
    }

    fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    fun  fieldsCheck(): Boolean {
        var isEmailValid = false
        var isPasswordValid = false
        var isNameValid = false
        var isSurnameValid = false
        var isNicknameValid = false
        var ok = false
        val name = editTextName.text.toString()
        val surname = editTextSurname.text.toString()
        val nickanme = editTextNickname.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Check for a valid email address.
        if (email.isEmpty()) {
            editTextEmail.setError(getString(R.string.email_error))
            isEmailValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError(resources.getString(R.string.error_invalid_email))
            isEmailValid = false
        } else {
            isEmailValid = true
        }

        // Check for a valid password.
        if (password.isEmpty()) {
            editTextPassword.setError(resources.getString(R.string.password_error))
            isPasswordValid = false
        } else if (password.length < 6) {
            editTextPassword.setError(resources.getString(R.string.error_invalid_password))
            isPasswordValid = false
        } else {
            isPasswordValid = true
        }

        //check others
        isNameValid = !name.isEmpty()
        isSurnameValid = !surname.isEmpty()
        isNicknameValid = !nickanme.isEmpty()

        //final check
        if (isEmailValid && isPasswordValid && isNameValid && isSurnameValid && isNicknameValid) {
            ok = true
            fs.createUserData(name, surname, nickanme, email, password)
            createUser(email, password)
        }
        return ok
    }
}