package edu.ufp.pam.examples.masterdetail.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import edu.ufp.pam.examples.masterdetail.dbcontacts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * This View Model keeps a reference to repository and an up-to-date list of all customers.
 * It acts as communication center between the Repository and UI.
 *
 * The ViewModel provides data in a lifecycle-conscious way to UI (survives configuration changes):
 * used to share data between instances of activity or between fragments.
 * Activities and fragments are responsible for drawing data to screen, while ViewModel takes care
 * of holding and processing the data needed for the UI.
 *
 * The ViewModel uses LiveData for changeable data to be displayed in UI:
 *  - The UI can use observer to receive data updates;
 *  - The Repository and UI are completely separated by the ViewModel;
 *  - The ViewModel does not make DB calls (all handled in Repository) making code more testable.
 *
 *
 * The viewModelScope property:
 *  In Kotlin, all coroutines run inside a CoroutineScope. The scope controls the lifetime of
 *  coroutines through its job (when canceling job of scope => cancels all its coroutines).
 *
 *  The AndroidX lifecycle-viewmodel-ktx library adds a *viewModelScope* extension function of
 *  ViewModel class that enables working with scopes.
 *
 *
 * WARNING: Do not keep a reference to a context that has a shorter lifecycle than the ViewModel
 * (e.g. Activity, Fragment, View... all these objects can be destroyed and recreated by OS
 * when there is a configuration change. Hence, it can cause a memory leaks.
 *
 * Use *AndroidViewModel* when need the application context (which has lifecycle that lives as long
 * as the application does).
 *
 * The ViewModels do not survive app process being killed in the background by OS.
 *
 */
class CustomersViewModel(app: Application) : AndroidViewModel(app) {

    private val repository: CustomersRepository

    // Use LiveData and cache customers returned by repository:
    //  - Put an observer on data fr UI to receive updates (instead of polling for changes).
    //  - The ViewModel separates the UI from the Repository.
    var allCustomers: LiveData<List<Customer>>

    init {
        //val customerDao = CustomersDatabase.getCustomerDatabaseInstance(application).customerDao()
        val customerDao =
            CustomersDatabase.getCustomerDatabaseInstance(app, viewModelScope).customerDao()
        repository = CustomersRepository(customerDao)
        allCustomers = repository.allCustomers
    }

    /** Launch new (non-blocking) coroutine to insert a customer */
    fun insert(customer: Customer) =
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(this.javaClass.simpleName,
                "launch(): async insert new customer ${customer}"
            )
            repository.insertCustomer(customer)
        }

    private val customers: MutableLiveData<List<Customer>> by lazy {
        MutableLiveData<List<Customer>>().also {
            loadUsers()
        }
    }

    fun getCustomers(): LiveData<List<Customer>> {
        return customers
    }

    /** Do an asynchronous operation to fetch customers */
    private fun loadUsers() {
        Log.e(
            this.javaClass.simpleName,
            "loadUsers(): going to load customer to DB..."
        )
        /* 1. Execute and AsynTask... deprecated! */
        //val existingCustomers: List<Customer> = LoadCustomersFromDatabaseAsyncTask().execute("").get()
        //customers.postValue(existingCustomers)

        /* 2. Create a new coroutine to move the execution off the UI thread */
        viewModelScope.launch(Dispatchers.IO) {
            Log.e(this.javaClass.simpleName,
                "launch(): loading all customers..."
            )
            val customerDao: CustomerDao = LoaderCustomersContentDatabase.getCustomerDao()
            //val allCustomers: LiveData<List<Customer>> = customerDao.loadAllCustomers()
            allCustomers = customerDao.loadAllCustomers()
        }
    }
}