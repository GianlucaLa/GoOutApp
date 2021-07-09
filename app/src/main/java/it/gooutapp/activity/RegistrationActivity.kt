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
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.firebase.FireStore
import kotlinx.android.synthetic.main.login.*
import kotlinx.android.synthetic.main.registration.*
import java.util.regex.Pattern

class RegistrationActivity: AppCompatActivity() {
    private val TAG = "REGISTRATION_ACTIVITY"
    private val fs : FireStore = FireStore()
    private val PASSWORD_PATTERN = Pattern.compile("^" + "(?=.*[@!?#$%^&+=])" + "(?=\\S+$)" + ".{6,}" + "$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.registration)
        regPB.visibility = View.INVISIBLE
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

    private fun createUser(email: String, password: String, name: String, surname: String, nickname: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    fs.createUserData(name, surname, nickname, email)
                    Log.d(TAG, "createUserWithEmail:success")
                    Toast.makeText(applicationContext, getString(R.string.successful_registration), Toast.LENGTH_SHORT).show()
                    closeActivity()
                } else {
                    // If sign in fails, display a message to the user.
                    regPB.visibility = View.INVISIBLE
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun CharSequence?.isValidEmail() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()
    private fun CharSequence?.isValidPassword() = !isNullOrEmpty() && PASSWORD_PATTERN.matcher(this).matches()

    fun fieldsCheck(view: View){
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
             showProgressBar()
            //controllo se già presente mail o nickname
            fs.checkForDuplicateUser(email, nickname) { duplicateMail, duplicateNick ->
                var msg = ""
                if (!duplicateMail && !duplicateNick){
                    createUser(email, password, name, surname, nickname)
                }else{
                    if(duplicateMail)
                        msg = "${resources.getString(R.string.duplicate_mail)}"
                    if(duplicateNick)
                        msg = "$msg\n${resources.getString(R.string.duplicate_nickname)}"
                }
                if(msg != ""){
                    hideProgressBar()
                    Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProgressBar() {
        regPB.visibility = View.VISIBLE
        imageViewRegDoodle.setColorFilter(resources.getColor(R.color.quantum_grey400))
        textViewTitle.setTextColor(resources.getColor(R.color.quantum_grey400))
        editTextNameView.isEnabled = false
        editTextSurnameView.isEnabled = false
        editTextNicknameView.isEnabled = false
        editTextEmailView.isEnabled = false
        editTextPasswordView.isEnabled = false
        RSignin.isEnabled = false
    }

    private fun hideProgressBar() {
        regPB.visibility = View.INVISIBLE
        imageViewRegDoodle.colorFilter = null
        textViewTitle.setTextColor(resources.getColor(R.color.colorPrimary))
        editTextNameView.isEnabled = true
        editTextSurnameView.isEnabled =  true
        editTextNicknameView.isEnabled = true
        editTextEmailView.isEnabled = true
        editTextPasswordView.isEnabled = true
        RSignin.isEnabled = true
    }
}