package edu.ufp.pam.examples.masterdetail

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.pamandroidkotlin.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import edu.ufp.pam.examples.masterdetail.dbcontacts.Customer
import edu.ufp.pam.examples.masterdetail.dbcontacts.LoaderCustomersContentDatabase
import edu.ufp.pam.examples.masterdetail.viewModel.CustomersViewModel
import kotlinx.android.synthetic.main.activity_main_customer_create_data_base.*
import kotlinx.android.synthetic.main.activity_new_customer.*
import java.lang.ref.WeakReference

class MainCustomerCreateDataBaseActivity : AppCompatActivity() {

    //Declare property for associated CustomersViewModel
    private lateinit var customersViewModel: CustomersViewModel

    //Code for communication between activities
    private val newCustomerActivityRequestCode = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_customer_create_data_base)

        // setSupportActionBar(findViewById(R.id.toolbar))

        val textViewListContacts = findViewById<TextView>(R.id.textViewListContacts)

        // Get new or existing CustomersViewModel from ViewModelProvider
        //customersViewModel = ViewModelProvider(this).get(CustomersViewModel::class.java)
        customersViewModel =
            ViewModelProvider(this,
                ViewModelProvider.AndroidViewModelFactory.getInstance(this.application)
            ).get(CustomersViewModel::class.java)

        // Add Observer for LiveData associated with customersViewModel.allCustomers
        // The onChanged() is triggered whenever observed data changes with activity in foreground.
        customersViewModel.allCustomers.observe(
            this,
            Observer { customers ->
                // Update cached list of customers
                customers?.let {
                    Log.e(this.javaClass.simpleName,
                        "onChanged(): customers.size=${customers.size}"
                    )
                    //Clear textViewListContacts
                    textViewListContacts.text=""
                    var i = 0
                    for (c in it) {
                        Log.e(this.javaClass.simpleName,
                            "onChanged(): customer[$i++]=${c}"
                        )
                        val item = "[${c.customerId}] ${c.customerName}"
                        textViewListContacts.append("${item} | ")
                    }
                }
            })

        addCustomerButtonMain.setOnClickListener {
            //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
            // .setAction("Action", null).show()

            //Create an Intent to launch the NewCustomerActivity
            val intent =
                Intent(
                    this@MainCustomerCreateDataBaseActivity,
                    NewCustomerActivity::class.java
                )
            startActivityForResult(intent, newCustomerActivityRequestCode)
        }

        //Use Create Button to create the DB and insert dummy test customers
        buttonCreateDatabaseCustomers.setOnClickListener {
            //Deprecated way...
            //use an AsyncTask to create the database and populate it with sample data
           // CustomersDatabaseAsyncTask(this@MainCustomerCreateDataBaseActivity)
              //  .execute("Dummy parameter string not useful for this task! :)")
        }
    }
/*
    ** When get back to Main Activity from NewCustomerActivity, checks if it returns RESULT_OK
    *  - then insert the new customer in database by calling
    *    the insert() method on ViewModel;
    *  - else show a Toast message.
    */
    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        Log.e(
            this.javaClass.simpleName,
            "onActivityResult(): back from NewCustomerActivity..."
        )
        if (requestCode == newCustomerActivityRequestCode && resultCode == Activity.RESULT_OK) {
            intentData?.let { data ->
                val customerName : String? =
                    data.getStringExtra(NewCustomerActivity.EXTRA_CUSTOMER_NAME_REPLY_KEY)
                Log.e(
                    this.javaClass.simpleName,
                    "onActivityResult(): new customer = $customerName"
                )
                val customerIndex : String? = customerName!!.subSequence(
                    customerName.length - 2,
                    customerName.length
                ).toString()
                //Customer(i, "Tio Patinhas $i", "Patinhas $i Lda", "Rua Sesamo $i", "Porto", "+35122000000$i")
                val customer = Customer(
                    0,
                    customerName,
                    "Patinhas ${customerIndex} Lda",
                    "Rua Sesamo $customerIndex",
                    "Porto",
                    "+35122000000$customerIndex",
                    null
                )
                customersViewModel.insert(customer)
                Unit
            }
        } else {
            Toast.makeText(applicationContext, "Empty customer... not saved!!", Toast.LENGTH_LONG).show()
        }
    }

    protected fun startFollowingActivity() {
        //Call CustomerItemContactDetailListActivity to list dababase content
        val intent = Intent(this, CustomerItemListActivity::class.java)
        //Pass any data to next activity
        intent.putExtra("CUSTOMER_ITEMS_SIZE", LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size)
        //Start your next activity
        startActivity(intent)
    }

    companion object {

        /**
         * class SomeTask : AsyncTask<Params, Progress, Result>
         *      Params: type of the parameters sent to doInBackground()
         *      Progress: type of the progress parameter sent to onProgressUpdate()
         *      Result: type of result of the background computation passed to onPostExecute().
         */
        class CustomersDatabaseAsyncTask(activity: MainCustomerCreateDataBaseActivity) :
            AsyncTask<String, Int, String>() {

            //private val parentActivity: CustomerItemContactDetailListActivity = activity
            private val activityReference: WeakReference<MainCustomerCreateDataBaseActivity> =
                WeakReference(activity)

            /**
             * Invoked on the UI thread before the task is executed.
             */
            override fun onPreExecute() {
                super.onPreExecute()
                Log.e(
                    this.javaClass.simpleName,
                    "onPreExecute(): going to do something before create CustomersDatabase..."
                )
                val activity = activityReference.get()
                if (activity == null || activity.isFinishing) return
                //...
            }

            /**
             * invoked on the background thread immediately after onPreExecute().
             */
            override fun doInBackground(vararg params: String?): String {
                Log.e(
                    this.javaClass.simpleName,
                    "doInBackground(): going to create CustomersDatabase, params = ${params[0]}"
                )
                val parentActivity = activityReference.get()
                Log.e(
                    this.javaClass.simpleName,
                    "doInBackground(): LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size = ${LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size}"
                )
                if (parentActivity != null && LoaderCustomersContentDatabase.CUSTOMER_ITEMS.size == 0) {
                    //parentActivity.buildCustomersContentDatabase.createDb(parentActivity.applicationContext)

                    //val context: Context = InstrumentationRegistry.getTargetContext()
                    LoaderCustomersContentDatabase.createDb(parentActivity.applicationContext)
                    this.publishProgress(50)
                    LoaderCustomersContentDatabase.addSampleItemsToDatabase()
                    this.publishProgress(100)
                    //return "OK"
                }
                return "OK"
            }

            /**
             * invoked on the UI thread after a call to publishProgress().
             */
            override fun onProgressUpdate(vararg progress: Int?) {
                val parentActivity = activityReference.get()
                if (parentActivity != null) {
                    Log.e(
                        this.javaClass.simpleName,
                        "onProgressUpdate(): Progress: ${progress[0]}%"
                    )
                    Toast.makeText(parentActivity, "Progress: ${progress[0]}%", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            /**
             * invoked on the UI thread after the background computation finishes.
             */
            override fun onPostExecute(result: String?) {
                super.onPostExecute(result)
                Log.e(
                    this.javaClass.simpleName,
                    "onPostExecute(): after creating CustomersDatabase, result=$result"
                )
                val parentActivity = activityReference.get()
                if (parentActivity != null && result == "OK") {
                    //parentActivity.setupRecyclerView(parentActivity.itemcontactdetail_list)
                    Toast.makeText(parentActivity, "Customers.db... Done!!", Toast.LENGTH_LONG)
                        .show()
                    parentActivity.startFollowingActivity()
                }
            }
        }
    }

}