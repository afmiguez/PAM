package edu.ufp.pam.examples.masterdetail.dbcontacts

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData


/**
 * A repository class abstracts access to multiple data sources and provides Façade for the app to
 * access those data sources. The repository is not part of the Architecture Components libraries,
 * but a suggested best practice for code separation.
 *
 * The Repository manages queries and allows to use multiple backends, e.g. it implements logic for
 * fetching data from a network or use results cached in a local database.
 *
 * Pass in the DAO (as a private property in the constructor) instead of the whole database.
 */
class CustomersRepository(private val customerDao: CustomerDao) {

    // Room executes all queries on a separate thread.
    // The public property is an observable LiveData which notifies  observer when  data changes.
    val allCustomers: LiveData<List<Customer>> = customerDao.loadAllCustomers()

    // Make this a suspend function so the caller knows this must be called on a non-UI thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insertCustomer(customer: Customer) {
        Log.e(
            this.javaClass.simpleName,
            "insertCustomer(): going to insert new customer ${customer}"
        )
        customerDao.insertCustomer(customer)
    }
}
