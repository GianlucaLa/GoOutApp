package it.gooutapp


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*


class RegistrationActivity: AppCompatActivity() {
    val fs : FireStore = FireStore()
    private val TAG = "REGISTRATION_ACTIVITY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        editTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                editTextPasswordView.endIconMode = TextInputLayout.END_ICON_PASSWORD_TOGGLE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
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

    fun  fieldsCheck(view: View){
        var isEmailValid = false
        var isPasswordValid = false
        var isNameValid = false
        var isSurnameValid = false
        var isNicknameValid = false
        val name = editTextName.text.toString()
        val surname = editTextSurname.text.toString()
        val nickname = editTextNickname.text.toString()
        val email = editTextEmail.text.toString()
        val password = editTextPassword.text.toString()

        // Check for a valid email address.
        when {
            email!!.isEmpty() -> {
                editTextEmailView.endIconMode = TextInputLayout.END_ICON_CLEAR_TEXT
                editTextEmail.error = resources.getString(R.string.email_error)
                isEmailValid = false
            }
            email.length < 6 -> {
                editTextEmail.error = resources.getString(R.string.error_invalid_email)
                isEmailValid = false
            }
            else -> {
                isEmailValid = true
            }
        }


        // Check for a valid password.
        when {
            password!!.isEmpty() -> {
                editTextPasswordView.endIconMode = TextInputLayout.END_ICON_NONE
                editTextPassword.error = resources.getString(R.string.password_error)
                isPasswordValid = false
            }
            password.length < 6 -> {
                editTextPasswordView.endIconMode = TextInputLayout.END_ICON_NONE
                editTextPassword.error = resources.getString(R.string.error_invalid_password)
                isPasswordValid = false
            }
            else -> {
                isPasswordValid = true
            }
        }

        //check others
        isNameValid = !name.isEmpty()
        isSurnameValid = !surname.isEmpty()
        isNicknameValid = !nickname.isEmpty()

        //final check
        if (isEmailValid && isPasswordValid && isNameValid && isSurnameValid && isNicknameValid) {
            fs.createUserData(name, surname, nickname, email)
            createUser(email, password)
        }else{
            Toast.makeText(applicationContext, getString(R.string.incorrect_fields_registration), Toast.LENGTH_SHORT).show()
        }
    }
}