package lab.wavgrafx.com.thinkingatheistcommunity

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
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
import kotlinx.android.synthetic.main.activity_reactions.*
import lab.wavgrafx.com.thinkingatheistcommunity.Model.ReactionModel
import java.text.SimpleDateFormat
import java.util.*
import android.text.method.ScrollingMovementMethod

class Reactions : AppCompatActivity() {

    lateinit var currentdatetime: String
    lateinit var reactions_editmessage: EditText
    lateinit var reactions_sendmessage: Button
    lateinit var currentuser: String

    lateinit var mAuth: FirebaseAuth
    lateinit var db: DocumentReference
    lateinit var rootRef: DocumentReference
    private var adapter: Reactions.MessengerFirestoreRecyclerAdapter? = null

    lateinit var rcdb: DocumentReference
    lateinit var rcCount: Any

    lateinit var alias: Any

    //    Declare Intent Data
    lateinit var intphone: String
    lateinit var intalias: String
    lateinit var intmessage: String
    lateinit var intdatetimesent: String
    lateinit var intreactioncount: String
    lateinit var intblocked: String
    lateinit var intuserblocked: String
    lateinit var intuserpenaltycount: String

    lateinit var messenger_alias: TextView
    lateinit var mesenger_message: TextView
    lateinit var messenger_datetime: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reactions)

        messenger_alias = findViewById(R.id.messenger_alias)
        mesenger_message = findViewById(R.id.mesenger_message)
        messenger_datetime = findViewById(R.id.messenger_datetime)

        val bundle:Bundle=intent.extras
        if(bundle!=null)
        {
            intphone = bundle.getString("phone")
            intalias = bundle.getString("alias")
            intmessage = bundle.getString("message")
            intdatetimesent = bundle.getString("datetimesent")

            intreactioncount = bundle.getString("reactionCount")
            intblocked = bundle.getString("blocked")

        }



        messenger_alias.text = "Posted by $intalias"
        mesenger_message.text = intmessage
        messenger_datetime.text = intdatetimesent.substring(0,16)

        mesenger_message.setMovementMethod(ScrollingMovementMethod())

        mAuth = FirebaseAuth.getInstance()
        currentuser = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        GetAlias()
        db = FirebaseFirestore.getInstance().document("Reactions/List")

        reactions_recyclerView.layoutManager = LinearLayoutManager(this)
        reactions_recyclerView.smoothScrollToPosition(0)
        reactions_editmessage = findViewById(R.id.reactions_editmessage)
        reactions_sendmessage = findViewById(R.id.reactions_sendmessage)
        reactions_sendmessage.setOnClickListener {
            sendMessage()
        }

        btn_back.setOnClickListener {
            finish()
        }






        currentuser = FirebaseAuth.getInstance().currentUser!!.phoneNumber.toString()
        mAuth = FirebaseAuth.getInstance()
        reactions_recyclerView.layoutManager = LinearLayoutManager(this)

        rootRef = FirebaseFirestore.getInstance().document("Reactions/List")
        val query = rootRef!!.collection("Message")
            .whereEqualTo("Post", "$intdatetimesent")
            .whereEqualTo("Blocked", "false")
            .orderBy("Timestamp", Query.Direction.DESCENDING)


        val options = FirestoreRecyclerOptions.Builder<ReactionModel>().setQuery(query, ReactionModel::class.java).build()
        adapter = MessengerFirestoreRecyclerAdapter(options)
        progressbar2.visibility = View.INVISIBLE


    }
    private inner class ReactionViewHolder internal constructor(private val view: View) : RecyclerView.ViewHolder(view) {
        internal fun setMessengerDetails(Phone: String, Alias: String, Reaction: String, DateTimeSent: String, Blocked: String) {


            val reactions_alias = view.findViewById<TextView>(R.id.reactions_alias)
            val reactions_message = view.findViewById<TextView>(R.id.reactions_message)
            val reactions_datetime = view.findViewById<TextView>(R.id.reactions_datetime)
            val userThumb = view.findViewById<TextView>(R.id.userThumb)


            reactions_alias.text = Alias
            reactions_message.text = Reaction
            reactions_datetime.text = DateTimeSent.substring(0,16)
            userThumb.text = Alias.substring(0,1)

        }



    }

    private inner class MessengerFirestoreRecyclerAdapter internal constructor(options: FirestoreRecyclerOptions<ReactionModel>) : FirestoreRecyclerAdapter<ReactionModel, ReactionViewHolder>(options) {
        override fun onBindViewHolder(reactionViewHolder: ReactionViewHolder, position: Int, reactionModel: ReactionModel) {



            reactionViewHolder.setMessengerDetails(reactionModel.Phone, reactionModel.Alias,
                reactionModel.Reaction, reactionModel.DateTimeSent, reactionModel.Blocked)


        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReactionViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(R.layout.reactions, parent, false)
            return ReactionViewHolder(view)


        }
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()

        reactions_recyclerView.adapter = adapter
        reactions_recyclerView.setHasFixedSize(true)
    }





    override fun onStop() {
        super.onStop()

        if (adapter != null) {
            adapter!!.stopListening()
        }


    }


    //----------------------------------------------------send message------------------------------------------------------------
    private fun sendMessage(){
        GetAlias()

        //---------------------------------Appointment Date Declarations---------------------------------

        currentdatetime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(System.currentTimeMillis())

        var cal = Calendar.getInstance()

        val myFormat = "yyyy-MM-dd HH:mm:ss.SSS" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        currentdatetime = sdf.format(cal.time)
//--------------------------------------------------------------------------------------------------

        reactions_editmessage = findViewById(R.id.reactions_editmessage)

        var reaction:String = reactions_editmessage.text.toString()
        var reactiondatetime:String = currentdatetime

        if (!reaction.isEmpty()) {
            try {
                reactions_editmessage.setText("")

                val items = HashMap<String, Any>()
                items.put("Phone", currentuser)
                items.put("Alias", alias)
                items.put("Reaction", reaction)
                items.put("DateTimeSent", reactiondatetime)
                items.put("Blocked", "false")
                items.put("Post", "$intdatetimesent")
                items.put("Timestamp", FieldValue.serverTimestamp())


                db.collection("Message").document(reactiondatetime).set(items).addOnSuccessListener {
                        void: Void? -> Toast.makeText(this, "Reaction posted", Toast.LENGTH_SHORT).show()
                    reactions_recyclerView.smoothScrollToPosition(0)
                    updateReactionCount()


                }.addOnFailureListener {
                        exception: java.lang.Exception -> Toast.makeText(this, exception.toString(), Toast.LENGTH_LONG).show()
                }
            }catch (e:Exception) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
            }
        }else {
            Toast.makeText(this, "Please type a reaction first.", Toast.LENGTH_LONG).show()
        }
    }


    fun GetAlias(){
        val adb = FirebaseFirestore.getInstance().document("TAC/Users")
        val query = adb.collection("List").whereEqualTo("Phone", "$currentuser")
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

    fun updateReactionCount(){
        GetAlias()
        rcdb = FirebaseFirestore.getInstance().document("Messenger/List")
        var rcInitial = rcdb.collection("Messages").document("$intdatetimesent")
        rcInitial.get()
        rcInitial.get().addOnCompleteListener(OnCompleteListener<DocumentSnapshot> { task ->
            if (task.isSuccessful) {
                val document = task.result!!.data
                rcCount = document!!.getValue("ReactionCount")
                var newcount = rcCount.toString().toInt()
                var newercount = ++newcount
                var updatedCount = newercount.toString()
                val items = HashMap<String, Any>()

                items.put("ReactionCount", "$updatedCount")
                rcdb = FirebaseFirestore.getInstance().document("Messenger/List")
                var rcrootRef = rcdb.collection("Messages").document("$intdatetimesent")
                    .update("ReactionCount", "$updatedCount")
                    .addOnSuccessListener {Toast.makeText(this, "Reaction count updated!", Toast.LENGTH_SHORT).show()}
                    .addOnFailureListener { e -> Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show() }
            } else {
                Toast.makeText(this, "There's an error accessing the internet.", Toast.LENGTH_SHORT).show()
            }
        })


    }

}
