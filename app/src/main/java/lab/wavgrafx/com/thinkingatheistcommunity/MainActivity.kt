package lab.wavgrafx.com.thinkingatheistcommunity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()
        if (mAuth.currentUser != null){
            val intent = Intent(this@MainActivity, Messenger::class.java)
            startActivity(intent)
            finish()
        }



        btnStart.setOnClickListener{

            if (checkbox_privacypolicy.isChecked == true){
                val intent = Intent(this@MainActivity,Login::class.java)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(this, "Please confirm you have read the Privacy Policy above.", Toast.LENGTH_LONG).show()
            }


        }

        privacypolicy.setOnClickListener{
            val url = "https://www.privacypolicies.com/privacy/view/619b898d3b93145e9d8fd9630014237a"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
    }

    override fun onStart() {
        super.onStart()
        checkConnectivity()
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
            Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT).show()
        }
    }
}