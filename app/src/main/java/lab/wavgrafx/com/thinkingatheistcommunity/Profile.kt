package lab.wavgrafx.com.thinkingatheistcommunity

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class Profile : AppCompatActivity() {

    lateinit var editText_Alias:EditText
    lateinit var btn_SaveAlias:Button
    lateinit var progressBar:ProgressBar
    lateinit var alias: String
    lateinit var txtphone:Any
    lateinit var mAuth: FirebaseAuth
    lateinit var db: DocumentReference
    lateinit var rootRef: DocumentReference
    lateinit var currentuser:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        mAuth = FirebaseAuth.getInstance()
        currentuser = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        GetAlias()

        editText_Alias = findViewById(R.id.editText_Alias)
        btn_SaveAlias = findViewById(R.id.btn_SaveAlias)
        progressBar = findViewById(R.id.progressBar)



        btn_SaveAlias.setOnClickListener {
            alias = editText_Alias.text.toString()
            if (alias.isEmpty()){
                Toast.makeText(this, "Please create a Username.", Toast.LENGTH_LONG).show()
            }else{
                checkConnectivity()

            }
        }
    }

    fun AliasCheck(){
        db = FirebaseFirestore.getInstance().document("TAC/Users")
        rootRef = db.collection("List").document("$alias")
        rootRef.get()
        rootRef.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result!!.data
                if (document != null) {
                    txtphone = document.getValue("Phone")

                    if (txtphone != "$currentuser"){
                        Toast.makeText(this, "Username is already taken.", Toast.LENGTH_SHORT).show()
                    }else{
                        startActivity(Intent(this, Messenger :: class.java))
                    }

                } else {
//                  No such alias
                    SaveAlias()
                }
            } else {
                Toast.makeText(this, "There's an error accessing the internet.", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun SaveAlias(){
        progressBar.setVisibility(View.VISIBLE)
        val any = try {
            val items = HashMap<String, Any>()
            items.put("Phone", currentuser)
            items.put("Alias", alias)
            items.put("Blocked", "false")
            items.put("PenaltyCount", "0")

            db = FirebaseFirestore.getInstance().document("TAC/Users")
            db.collection("List").document("$alias").set(items).addOnSuccessListener { void: Void? ->
                Toast.makeText(this, "Username has been saved.", Toast.LENGTH_LONG).show()
                progressBar.setVisibility(View.INVISIBLE)
                val intent = Intent(this@Profile, Messenger::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener { exception: java.lang.Exception ->
                Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    private fun checkConnectivity(){
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        val isConnected = activeNetwork != null && activeNetwork.isConnected
        if (!isConnected){
            Toast.makeText(this, "Check network connection", Toast.LENGTH_SHORT).show()
        }else{
            AliasCheck()
        }
    }

    fun GetAlias(){
        db = FirebaseFirestore.getInstance().document("TAC/Users")
        val query = db.collection("List").whereEqualTo("Phone", "$currentuser")
        query.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
            if (task.isSuccessful) {
                if (task.result!!.isEmpty){
                    Toast.makeText(this, "Please create a Username", Toast.LENGTH_LONG).show()
                }else{
                    val intent = Intent(this@Profile, Messenger::class.java)
                    startActivity(intent)
                    finish()
                }

            } else {
                Toast.makeText(this, "Please create a Username", Toast.LENGTH_LONG).show()
            }
        })
    }
}

