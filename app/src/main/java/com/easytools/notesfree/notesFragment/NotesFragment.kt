package com.easytools.notesfree.notesFragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.easytools.notesfree.*
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import kotlinx.android.synthetic.main.fragment_notes.*
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

class NotesFragment : Fragment(R.layout.fragment_notes), NotesInterface {
    private lateinit var viewModel: NotesViewModel
    private var mActionMode: ActionMode? = null
    lateinit var adapter: NotesAdapter
    private val selectedNotes = mutableListOf<Notes>()
    //ads-----
    private var mInterstitialAd: InterstitialAd? = null
    private final var TAG = "MainActivity"
    lateinit var adRequest: AdRequest
    var internetExistence = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //ads-----
        initializeAndLoadAdsForBeginningFragment()
        //ads-----
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)
        //setting the recyclerview
        adapter = NotesAdapter(this)
        rvNotes.adapter = adapter
        rvNotes.layoutManager = LinearLayoutManager(this.context)
        //observe the changing of the database
        viewModel.allNotes.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })

        fabAddNote.setOnClickListener {
            val intent = Intent(this.context, NewNoteActivity::class.java)
            //startActivity(intent)
            loadAndShowAds(intent)

            if(mActionMode != null) {
                mActionMode!!.finish()
            }
        }

        fabAddSpeech.setOnClickListener {
            requestRecordAudioPermission()
            if( hasRecordAudioPermission() ) {
                MyApplication.isSpeechToTextFabPressed = "true"
                val intent = Intent(this.context, NewNoteActivity::class.java)
                //startActivity(intent)
                loadAndShowAds(intent)

                if(mActionMode != null) {
                    mActionMode!!.finish()
                }
            }
        }

    }

    override fun deleteNote(allSelectedNotes: List<Notes>) {
        var message =
            if(selectedNotes.size == 1) "Do you want to delete 1 item?"
            else "Do you want to delete ${selectedNotes.size} items?"
        //---------------------AlertDialog------------------------
        val addContactDialog = AlertDialog.Builder(this.requireContext())
            .setTitle("Delete") //=========
            .setMessage(message) //==========
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                for(sItem in allSelectedNotes) {
                    viewModel.delete(sItem)
                }
                if(selectedNotes.size == 1)
                    Toast.makeText(this.context, "Item Deleted", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this.context, "Items Deleted", Toast.LENGTH_SHORT).show()
                selectedNotes.clear()
                adapter.updateForRecolor() //hence if you clicked yes, the selection will be removed
                mActionMode!!.finish()
            }
            .setNegativeButton("No") { _, _ ->
            }
            .setOnDismissListener {
            }
            .create()
        addContactDialog.show()
        //---------------------AlertDialog------------------------
    }

    override fun updateNote(note: Notes) {
        val id = note.id
        val intent = Intent(this.context, NewNoteActivity::class.java)
        intent.putExtra("EXTRA_ID", id)
        intent.putExtra("EXTRA_TITLE", note.title)
        intent.putExtra("EXTRA_TEXT", note.text)
        intent.putExtra("EXTRA_ITEMCOLOR", note.itemColor)
        intent.putExtra("EXTRA_CREATION_DATE", note.creationDate)

        //startActivity(intent)
        loadAndShowAds(intent)
    }

    //----permission------
    private fun hasRecordAudioPermission() =
        ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
    //---
    private fun requestRecordAudioPermission() {
        var permissionsToRequest = mutableListOf<String>()

        if(!hasRecordAudioPermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if(permissionsToRequest.isNotEmpty()){
            requestPermissions(permissionsToRequest.toTypedArray(), 0)
        }
    }
    //----permission------

    //====================================
    //actionMode------------------
    override fun showContextualActionMode(note: Notes) {
        MyApplication.isActionModeActive = true
        if(mActionMode == null){
            mActionMode = activity?.startActionMode(mActionModeCallback(note))
            selectedNotes.add(note)
        }
        else {
            if (selectedNotes.contains(note)) { //so this means that the user want to unselect this item
                selectedNotes.remove(note)
                if(selectedNotes.size == 0) {
                    mActionMode!!.finish() //to if you unselected the last selected item, actionMode professionally should be destroyed
                }
            } else {
                selectedNotes.add(note)  //so this means that the user want to select this item
            }
        }
    }

    private fun mActionModeCallback(note: Notes) = object : ActionMode.Callback{  //remove this  note: Notes
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.actionmode_menu, menu)
            mode?.title = "Choose Action"

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            when(item?.itemId) {
                R.id.miDeleteItem -> {
                    if(selectedNotes.size != 0)
                        deleteNote(selectedNotes)
                    return true
                }
                R.id.miShareItem -> {
                    if(selectedNotes.isNotEmpty()){
                        val intent = Intent(Intent.ACTION_SEND)
                        intent.setType("text/plain")
                        if(selectedNotes.size == 1) {
                            var singleNote = selectedNotes[0]
                            intent.putExtra(Intent.EXTRA_TEXT, "${singleNote.title}\n${singleNote.text}")
                        }
                        else if(selectedNotes.size > 1) {
                            var textOfAllSelectedNotes = ""
                            for(item in selectedNotes) {
                                textOfAllSelectedNotes =
                                    if(textOfAllSelectedNotes.isEmpty())
                                        "${item.title}\n${item.text}"
                                    else
                                        textOfAllSelectedNotes + "\n---------\n${item.title}\n${item.text}"
                            }
                            intent.putExtra(Intent.EXTRA_TEXT, textOfAllSelectedNotes)
                        }
                        val chooser = Intent.createChooser(intent, null)
                        startActivity(chooser)
                    }
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            MyApplication.isActionModeActive = false
            mActionMode = null
            selectedNotes.clear() //when the actionMode is destroyed, clear the list of selectedNotes
            adapter.updateForRecolor()
        }
    }
    //-----------------

    override fun onDestroy() { //it is called when navigating to another fragment
        super.onDestroy()
        if(mActionMode != null) {
            mActionMode!!.finish()  //to destroy actionMode when navigating to another fragment
        }
    }
    //actionMode------------------

    //check internet-------------------------
    private fun checkInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    //check internet-------------------------
    //load ads for beginning the fragment-------------
    private fun initializeAndLoadAdsForBeginningFragment() {
        //ads---------
        if(checkInternet(this.requireContext())) internetExistence = true
        MobileAds.initialize(this.requireContext()) {}
        adRequest = AdRequest.Builder().build()
        //the ad must be loaded each time you want to show  so you must repeat this method manually
        InterstitialAd.load(this.requireContext(),"paste your adUnitId", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                //Log.d(TAG, adError?.toString())
                mInterstitialAd = null
            }
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                //Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })
        //ads---------
    }
    //load ads for beginning the fragment-------------
    //load and show ads--------------------------
    private fun loadAndShowAds(intent: Intent) {
        //ads---------
        if(!checkInternet(this.requireContext())) {
            internetExistence = false //this for if user opened the app with internet then disconnected and moved to activities in it then reconnected
        }
        //for in case of the user connected to internet after opening the app
        if(!internetExistence) {
            if(checkInternet(this.requireContext())) {
                internetExistence = true
                InterstitialAd.load(this.requireContext(),"//paste your adUnitId", adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        //Log.d(TAG, adError?.toString())
                        mInterstitialAd = null
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        //Log.d(TAG, "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
            }
        }
        //listener to the ad events
        //this listener works if only that mInterstitialAd was not null.  and this mInterstitialAd must be initialized in the first time in the overridden
        // method onAdLoaded that needs internet connection
        mInterstitialAd?.fullScreenContentCallback = object: FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                Log.d(TAG, "Ad dismissed fullscreen content.")
                mInterstitialAd = null
                startActivity(intent)

                //load ad again to ready for the next time
                InterstitialAd.load(requireContext(),"//paste your adUnitId", adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        //Log.d(TAG, adError?.toString())
                        mInterstitialAd = null
                    }
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        //Log.d(TAG, "Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                })
            }
        }

        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this.requireActivity())
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.")
            startActivity(intent)
        }
        //ads---------
    }
    //load and show ads--------------------------

//------------------------------------------------------------------------
}

