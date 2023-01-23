package com.easytools.notesfree.photosFragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
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
import kotlinx.android.synthetic.main.fragment_photos.*

class PhotosFragment : Fragment(R.layout.fragment_photos), PhotosInterface {
    private lateinit var viewModel: PhotosViewModel
    private var mActionMode: ActionMode? = null
    lateinit var adapter: PhotosAdapter
    private val selectedPhotos = mutableListOf<Photos>()
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
        viewModel = ViewModelProvider(this).get(PhotosViewModel::class.java)

        adapter = PhotosAdapter(this)
        rvPhotos.adapter = adapter
        rvPhotos.layoutManager = LinearLayoutManager(this.context)

        val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
            if(it != null) {
                val photo = Photos(it)
                viewModel.insert(photo)
            }
        }

        fabTakePhoto.setOnClickListener {
            if(mActionMode != null) {
                mActionMode!!.finish()
            }
            takePhoto.launch()
        }

        viewModel.allPhotos.observe(viewLifecycleOwner, Observer {
            adapter.update(it)
        })

    }

    override fun deletePhoto(allSelectedPhotos: List<Photos>) {
        var message = //============
            if(selectedPhotos.size == 1) "Do you want to delete 1 item?"
            else "Do you want to delete ${selectedPhotos.size} items?"
        //---------------------AlertDialog------------------------
        val addContactDialog = AlertDialog.Builder(this.requireContext())
            .setTitle("Delete")
            .setMessage(message)
            .setIcon(R.drawable.ic_delete)
            .setPositiveButton("Yes") { _, _ ->
                for(sItem in allSelectedPhotos) {
                    viewModel.delete(sItem)
                }
                if(selectedPhotos.size == 1)
                    Toast.makeText(this.context, "Item Deleted", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(this.context, "Items Deleted", Toast.LENGTH_SHORT).show()
                selectedPhotos.clear()
                adapter.updateForRecolor() //hence if you clicked yes, the selection will be removed
                mActionMode!!.finish()
            }
            .setNegativeButton("No") { _, _ ->
            }.create()
        addContactDialog.show()
        //---------------------AlertDialog------------------------
    }

    override fun fullScreen(bitmap: Bitmap) {
        val intent = Intent(this.context, FullscreenActivity::class.java)
        intent.putExtra("EXTRA_BITMAP", bitmap)
        //startActivity(intent)
        loadAndShowAds(intent)
    }

    //-----------------

    //------permission------
    private fun hasWriteExternalStoragePermission() =
        ActivityCompat.checkSelfPermission(this.requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    //-----
    private fun requestWriteExternalStoragePermission() {
        var permissionsToRequest = mutableListOf<String>()

        if(!hasWriteExternalStoragePermission()) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(permissionsToRequest.isNotEmpty()){
            requestPermissions(permissionsToRequest.toTypedArray(), 0)
        }
    }
    //------permission------

    //====================================
    //actionMode------------------
    override fun showContextualActionMode(photo: Photos) {
        MyApplication.isActionModeActive = true
        if(mActionMode == null){
            mActionMode = activity?.startActionMode(mActionModeCallback(photo))
            selectedPhotos.add(photo)
        }
        else {
            if (selectedPhotos.contains(photo)) { //so this means that the user want to unselect this item
                selectedPhotos.remove(photo)
                if(selectedPhotos.size == 0) {
                    mActionMode!!.finish() //to if you unselected the last selected item, actionMode professionally should be destroyed
                }
            } else {
                selectedPhotos.add(photo)  //so this means that the user want to select this item
            }
        }
    }

    private fun mActionModeCallback(photo: Photos) = object : ActionMode.Callback{
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
                    if(selectedPhotos.size != 0)
                        deletePhoto(selectedPhotos)
                    return true
                }
                R.id.miShareItem -> {
                    if(selectedPhotos.isNotEmpty()){
                        requestWriteExternalStoragePermission()
                        if(hasWriteExternalStoragePermission()) {
                            var bitmap = selectedPhotos[0].bitmap
                            var bitmapPath = MediaStore.Images.Media.insertImage(activity?.contentResolver, bitmap, "palette", "share palette");
                            var bitmapUri = Uri.parse(bitmapPath);

                            val intent = Intent(Intent.ACTION_SEND);
                            intent.setType("image/png");
                            intent.putExtra(Intent.EXTRA_STREAM, bitmapUri);
                            val chooser = Intent.createChooser(intent, "Share Photo")
                            startActivity(chooser)
                        }
                    }
                    return true
                }
                else -> return false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            MyApplication.isActionModeActive = false
            mActionMode = null
            selectedPhotos.clear() //when the actionMode is destroyed, clear the list of selectedPhotos
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

//------------------------------------------------------------------------
}
