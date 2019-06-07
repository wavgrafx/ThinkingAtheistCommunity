package lab.wavgrafx.com.thinkingatheistcommunity

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.hbb20.CountryCodePicker
import kotlinx.android.synthetic.main.activity_login.*
import java.util.concurrent.TimeUnit

class Login : AppCompatActivity() {

    lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var mAuth: FirebaseAuth
    lateinit var cpp: CountryCodePicker
    lateinit var editTextPhone: EditText
    var verificationId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mAuth = FirebaseAuth.getInstance()

        cpp = findViewById(R.id.ccp)
        editTextPhone = findViewById(R.id.editTextPhone)
        cpp.registerCarrierNumberEditText(editTextPhone)

        ccp.setPhoneNumberValidityChangeListener(CountryCodePicker.PhoneNumberValidityChangeListener {
            if (it == false){editTextPhone.setTextColor(Color.parseColor("#FF0000"))}
            else{editTextPhone.setTextColor(Color.parseColor("#FFFFFF"))}
        })


        btnVerify.setOnClickListener { view: View? -> progressBar.visibility = View.VISIBLE

            if (cpp.isValidFullNumber == true){
                verify()

            }else{
                Toast.makeText(this, "Please type in a valid mobile number", Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.INVISIBLE

            }
        }


        btnConfirm.setOnClickListener { view: View? -> progressBar.visibility = View.INVISIBLE
            var codeConfirmer:Int = editTextCode.length().toInt()

            if (codeConfirmer == 6){
                authenticate()
            }else{
                Toast.makeText(this, "Please type in the 6-digit code sent by SMS.", Toast.LENGTH_SHORT)
                    .show()
                progressBar.visibility = View.INVISIBLE
            }

        }

        checkConnectivity()

    }



    override fun onStart() {
        super.onStart()
        if (mAuth.currentUser != null){
            startActivity(Intent(this, Messenger :: class.java))
        }





    }

    private fun verificationCallbacks(){
        mCallbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                progressBar.visibility = View.INVISIBLE
                signIn(credential)
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                return
            }

            override fun onCodeSent(verification: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(verification, p1)
                verificationId = verification.toString()
                progressBar.visibility = View.INVISIBLE
                textViewPINConfirm.visibility = View.VISIBLE
                editTextCode.visibility = View.VISIBLE
                btnConfirm.visibility = View.VISIBLE
            }

        }
    }

    private fun verify(){
        verificationCallbacks()


        val phone = cpp.fullNumberWithPlus

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phone,
            60,
            TimeUnit.SECONDS,
            this,
            mCallbacks
        )
    }

    private fun signIn(credential: PhoneAuthCredential){
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener {
                    task : Task<AuthResult> ->
                if (task.isSuccessful){
                    toast("Logged in Successfully")
                    finish()
                    startActivity(Intent(this, Profile::class.java))
                } else{
                    toast("PIN Code is invalid.")
                }
            }
    }

    private fun authenticate(){
        val verifiNo = editTextCode.text.toString()

        val credential:PhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId, verifiNo)

        signIn(credential)

    }

    private fun toast(msg: String){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }

    var backButtonCount:Int = 0

    override fun onBackPressed() {

        if (backButtonCount >= 1) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT)
                .show()
            backButtonCount++
        }
    }

    private fun checkConnectivity(){
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected
        if (!isConnected){
            Toast.makeText(this, "Connect to a WIFI or Switch on Data to Login", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        checkConnectivity()
    }
}
