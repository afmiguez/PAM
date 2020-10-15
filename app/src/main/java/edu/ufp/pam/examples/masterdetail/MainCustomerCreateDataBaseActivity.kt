package edu.ufp.pam.examples.masterdetail

import android.content.Intent
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.pamandroidkotlin.R
import edu.ufp.pam.examples.masterdetail.dbcontacts.LoaderCustomersContentDatabase
import kotlinx.android.synthetic.main.activity_main_customer_create_data_base.*
import java.lang.ref.WeakReference

class MainCustomerCreateDataBaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_customer_create_data_base)

        buttonCreateDatabaseCustomers.setOnClickListener {
            //Use an AsyncTask to create the database and populate it with sample data
            CustomersDatabaseAsyncTask(this@MainCustomerCreateDataBaseActivity).execute("Nothing useful for this task! :)")

            /*
            val replyIntent = Intent()
            val EXTRA_REPLY = "com.example.android.wordlistsql.REPLY"
            replyIntent.putExtra(EXTRA_REPLY, "Teste")
            setResult(Activity.RESULT_OK, replyIntent)
            finish()
             */
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