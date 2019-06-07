package lab.wavgrafx.com.thinkingatheistcommunity

import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import kotlinx.android.synthetic.main.activity_messenger.*
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.messages.view.*
import lab.wavgrafx.com.thinkingatheistcommunity.Model.MessengerModel
import java.text.SimpleDateFormat
import java.util.*


class Messenger : AppCompatActivity() {

    lateinit var currentdatetime: String
    lateinit var messenger_editmessage: EditText
    lateinit var messenger_sendmessage: Button
    lateinit var currentuser: String

    lateinit var mAuth: FirebaseAuth
    lateinit var db: DocumentReference
    lateinit var rootRef: DocumentReference
    private var adapter: Messenger.MessengerFirestoreRecyclerAdapter? = null

    lateinit var alias: Any

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messenger)

        mAuth = FirebaseAuth.getInstance()
        currentuser = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        GetAlias()
        db = FirebaseFirestore.getInstance().document("Messenger/List")

        messenger_recyclerView.layoutManager = LinearLayoutManager(this)
        messenger_recyclerView.smoothScrollToPosition(0)
        messenger_editmessage = findViewById(R.id.messenger_editmessage)
        messenger_sendmessage = findViewById(R.id.messenger_sendmessage)




        messenger_sendmessage.setOnClickListener {
            sendMessage()

        }

        btn_logout.setOnClickListener{
            SignoutAlertDialog()
        }

        btn_mypost.setOnClickListener {
            startActivity(Intent(this, MyPost::class.java))
        }




        currentuser = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        mAuth = FirebaseAuth.getInstance()
        messenger_recyclerView.layoutManager = LinearLayoutManager(this)

        rootRef = FirebaseFirestore.getInstance().document("Messenger/List")
        val query = rootRef!!.collection("Messages")
            .whereEqualTo("Blocked", "false")
            .orderBy("Timestamp", Query.Direction.DESCENDING)

        val options = FirestoreRecyclerOptions.Builder<MessengerModel>().setQuery(query, MessengerModel::class.java).build()
        adapter = MessengerFirestoreRecyclerAdapter(options)




    }
    private inner class MessengerViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        internal fun setMessengerDetails(Phone: String, Alias: String, Message: String, DateTimeSent: String, ReactionCount: String, Blocked: String) {


            val messenger_alias = view.findViewById<TextView>(R.id.messenger_alias)
            val mesenger_message = view.findViewById<TextView>(R.id.mesenger_message)
            val messenger_datetime = view.findViewById<TextView>(R.id.messenger_datetime)
            val userThumb = view.findViewById<TextView>(R.id.userThumb)
            val txt_rccount = view.findViewById<TextView>(R.id.txt_rccount)

            messenger_alias.text = Alias
            mesenger_message.text = Message
            messenger_datetime.text = DateTimeSent.substring(0,16)
            userThumb.text = Alias.substring(0,1)
            txt_rccount.text = "($ReactionCount)"

            progressbar1.visibility = View.INVISIBLE



            view.btn_reaction.setOnClickListener{
                val intent = Intent(this@Messenger, Reactions::class.java)
                intent.putExtra("phone", "$Phone")
                intent.putExtra("alias", "$Alias")
                intent.putExtra("message", "$Message")
                intent.putExtra("datetimesent", "$DateTimeSent")
                intent.putExtra("reactionCount", "$ReactionCount")
                intent.putExtra("blocked", "$Blocked")
                startActivity(intent)
            }



        }

    }

    private inner class MessengerFirestoreRecyclerAdapter internal constructor(options: FirestoreRecyclerOptions<MessengerModel>) : FirestoreRecyclerAdapter<MessengerModel, MessengerViewHolder>(options) {
        override fun onBindViewHolder(messengerViewHolder: MessengerViewHolder, position: Int, messengerModel: MessengerModel) {



            messengerViewHolder.setMessengerDetails(messengerModel.Phone, messengerModel.Alias,
                messengerModel.Message, messengerModel.DateTimeSent, messengerModel.ReactionCount, messengerModel.Blocked)


        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessengerViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.messages, parent, false)
            return MessengerViewHolder(view)


        }
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()

        messenger_recyclerView.adapter = adapter
        messenger_recyclerView.setHasFixedSize(true)
        progressbar1.visibility = View.INVISIBLE
    }




    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }


    }


    //----------------------------------------------------send message------------------------------------------------------------
    private fun sendMessage(){


        //---------------------------------Appointment Date Declarations---------------------------------

        currentdatetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())

        var cal = Calendar.getInstance()

        val myFormat = "yyyy-MM-dd HH:mm:ss.SSS" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        currentdatetime = sdf.format(cal.time)
//--------------------------------------------------------------------------------------------------

        messenger_editmessage = findViewById(R.id.messenger_editmessage)

        var message:String = messenger_editmessage.text.toString()
        var messagedatetime:String = currentdatetime

        if (!message.isEmpty()) {
            try {
                messenger_editmessage.setText("")


                val items = HashMap<String, Any>()
                items.put("Phone", currentuser)
                items.put("Alias", alias)
                items.put("Message", message)
                items.put("DateTimeSent", messagedatetime)
                items.put("ReactionCount", "0")
                items.put("Blocked", "false")
                items.put("Timestamp", FieldValue.serverTimestamp())


                db.collection("Messages").document(messagedatetime).set(items).addOnSuccessListener {
                        void: Void? -> Toast.makeText(this, "Message posted", Toast.LENGTH_SHORT).show()
                    messenger_recyclerView.smoothScrollToPosition(0)



                }.addOnFailureListener {
                        exception: java.lang.Exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }
            }catch (e:Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }else {
            Toast.makeText(this, "Please type a message first.", Toast.LENGTH_LONG).show()
        }
    }


    fun GetAlias(){
        db = FirebaseFirestore.getInstance().document("TAC/Users")
        val query = db.collection("List").whereEqualTo("Phone", "$currentuser")
        query.get().addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
            if (task.isSuccessful) {
                if (task.result!!.isEmpty){
                    startActivity(Intent(this, Profile::class.java))
                    finish()
                }else{
                    val document = task.result!!.documents[0].data
                    if (document != null) {
                        alias = document.getValue("Alias")
                        textview_username.text = alias.toString()
                    }else{
                        Toast.makeText(this, "Unable to access data.", Toast.LENGTH_LONG).show()
                    }
                }

            } else {
                startActivity(Intent(this, Profile::class.java))
                finish()
            }
        })
    }


    private fun signOut(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@Messenger,Login::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
        finish()

    }

    private fun SignoutAlertDialog(){
        lateinit var dialog: AlertDialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Log out")
        builder.setMessage("Do you want to logout?")
        val dialogClickListener = DialogInterface.OnClickListener{ _, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE -> signOut()
                DialogInterface.BUTTON_NEGATIVE -> dialog.hide()
            }
        }
        builder.setPositiveButton("YES",dialogClickListener)
        builder.setNegativeButton("NO",dialogClickListener)
        dialog = builder.create()
        dialog.show()
    }
}
