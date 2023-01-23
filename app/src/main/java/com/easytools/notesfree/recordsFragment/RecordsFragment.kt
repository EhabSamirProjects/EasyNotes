package com.easytools.notesfree.recordsFragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.fragment_records.*
import kotlinx.android.synthetic.main.fragment_records.vBackground
import kotlinx.android.synthetic.main.record_item.*
import java.io.File
import java.util.*

class RecordsFragment : Fragment(R.layout.fragment_records), RecordsInterface {

    private lateinit var viewModel: RecordsViewModel
    var mediaPlayer = MediaPlayer()
    var isPlaying = true
    var isPaused = false
    var previousFilePath = ""
    lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var itemColor = ""
    private var mActionMode: ActionMode? = null
    lateinit var adapter: RecordsAdapter
    private val selectedRecords = mutableListOf<Records>()
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
        bottomSheetBehavior = BottomSheetBehavior.from(clBottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        vBackground.visibility = View.GONE

        viewModel = ViewModelProvider(this).get(RecordsViewModel::class.java)

        adapter = RecordsAdapter(this)
        rvRecords.adapter = adapter
        rvRecords.layoutManager = LinearLayoutManager(this.context)

        viewModel.allRecords.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })

        fabStartRecording.setOnClickListener {
            requestRecordAudioPermission()
            if( hasRecordAudioPermission() ) {
                val intent = Intent(this.context, NewRecordActivity::class.java)
                //startActivity(intent)
                loadAndShowAds(intent)
            }
            if(mActionMode != null) {
                mActionMode!!.finish()
            }
        }

    }

    override fun playRecord(record: Records) {

        if(record.filePath != previousFilePath) {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(record.filePath)
            mediaPlayer.prepare()
            previousFilePath = record.filePath
        }

        if(!mediaPlayer.isPlaying) {
            mediaPlayer.start()
            ibPlay.setImageResource(R.drawable.ic_pause)
        }
        else if(mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            ibPlay.setImageResource(R.drawable.ic_play)
        }
    }

    override fun deleteRecord(allSelectedRecords: List<Records>) {
        var message = //============
            if(selectedRecords.size == 1) "Do you want to delete 1 item?"
            else "Do you want to delete ${selectedRecords.size} items?"
        //---------------------AlertDialog------------------------
        val addContactDialog = AlertDialog.Builder(this.requireContext())
            .setTitle("Delete")
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                for(sItem in allSelectedRecords) {
                    viewModel.delete(sItem)
                }
                if(selectedRecords.size == 1)
                    Toast.makeText(this.context, "Item Deleted", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this.context, "Items Deleted", Toast.LENGTH_SHORT).show()
                selectedRecords.clear()
                adapter.updateForRecolor() //hence if you clicked yes, the selection will be removed
                mActionMode!!.finish()
            }
            .setNegativeButton("No") { _, _ ->

            }.create()
        addContactDialog.show()
        //---------------------AlertDialog------------------------
    }

    override fun updateRecord(record: Records) {
        btnCancelRecord.setText("Cancel")
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        vBackground.visibility = View.VISIBLE
        etRecordName.setText(record.filename)
        itemColor = record.itemColor
        if(itemColor != "")
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        else {
            if(MyApplication.currentMode == "0") {
                clBottomSheet.setBackgroundColor(Color.parseColor("#FF000000"))
            } else
                clBottomSheet.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
        }
        //---------
        btnSaveRecord.setOnClickListener {
            var recordName = etRecordName.text.toString()
            var filename = ""
            if(recordName.isNotEmpty()) {
                filename = etRecordName.text.toString()
            } else {
                filename = record.filename
            }

            var record = Records(record.id, filename, record.filePath, record.duration, itemColor, record.date)
            viewModel.insert(record)

            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            vBackground.visibility = View.GONE
        }
        btnCancelRecord.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            vBackground.visibility = View.GONE
        }
        //-------------------------------
        fabWhite.setOnClickListener{ //fabNoColor
            itemColor = ""
            var noColor = ""
            if(MyApplication.currentMode == "0") {//MODE_NIGHT_YES
                noColor = "#FF000000"
            } else
                noColor = "#FFFFFFFF"

            clBottomSheet.setBackgroundColor(Color.parseColor(noColor))
        }
        fabGrey.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.grey)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrange.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.orange)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabBlue.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.blue)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.green)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabYellow.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.yellow)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabLightRed.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.light_red)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrchid.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.orchid)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabSpringGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this.requireContext(), R.color.spring_green)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        //-------------------------------
    }
    //-------------------------------------------------------------

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
    override fun showContextualActionMode(record: Records) {
        MyApplication.isActionModeActive = true
        if(mActionMode == null){
            mActionMode = activity?.startActionMode(mActionModeCallback(record))
            selectedRecords.add(record)
        }
        else {
            if (selectedRecords.contains(record)) { //so this means that the user want to unselect this item
                selectedRecords.remove(record)
                if(selectedRecords.size == 0) {
                    mActionMode!!.finish() //to if you unselected the last selected item, actionMode professionally should be destroyed
                }
            } else {
                selectedRecords.add(record)  //so this means that the user want to select this item
            }
        }
    }

    private fun mActionModeCallback(record: Records) = object : ActionMode.Callback{
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
                    if(selectedRecords.size != 0)
                        deleteRecord(selectedRecords)
                    return true
                }
                R.id.miShareItem -> {
                    if(selectedRecords.isNotEmpty()){
                        val share = Intent(Intent.ACTION_SEND)
                        share.setType("audio/*");
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                        //share single record these days currently
                        var singleRecord = selectedRecords[0]
                        share.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(requireContext(),
                            context?.applicationContext?.getPackageName() + ".my.package.name.provider",
                            File(Objects.requireNonNull(singleRecord.filePath))
                        ))

                        val chooser = Intent.createChooser(share, "Share Record")
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
            selectedRecords.clear() //when the actionMode is destroyed, clear the list of selectedRecords
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

//------------------------------------------------------------------------------
}

