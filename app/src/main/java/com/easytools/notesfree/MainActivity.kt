package com.easytools.notesfree

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuItemCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.createDataStore
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.easytools.notesfree.notesFragment.NotesFragment
import com.easytools.notesfree.photosFragment.PhotosFragment
import com.easytools.notesfree.recordsFragment.RecordsFragment
import com.easytools.notesfree.todoFragment.TodoFragment
import kotlinx.android.synthetic.main.action_view_checkbox.view.*
import kotlinx.android.synthetic.main.action_view_more_horiz.view.*
import kotlinx.android.synthetic.main.action_view_switch.view.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var dataStore: DataStore<Preferences>
    lateinit var toggle: ActionBarDrawerToggle
    var onCreate = true
    var lifecycle = 0  //this for reset the fragment when exiting by back key and didn't destroy it from recent apps
    // because what happening is: when you open the app again -> it choose the first fragment but the selected bottom bar item is the old fragment
    // so I use this var and  MyApplication.f (f means fragment) to solve this problem
    var switchMode = 0

    val notesFragment = NotesFragment()
    val todoFragment = TodoFragment()
    val recordsFragment = RecordsFragment()
    val photosFragment = PhotosFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(MyApplication.whichFrag == 0) {
            replaceFragment(notesFragment)
        }
        else if(MyApplication.whichFrag == 1) {
            replaceFragment(notesFragment)
        }
        else if(MyApplication.whichFrag == 2) {
            replaceFragment(todoFragment)
        }
        else if(MyApplication.whichFrag == 3) {
            replaceFragment(recordsFragment)
        }
        else if(MyApplication.whichFrag == 4) {
            replaceFragment(photosFragment)
        }

        lifecycle = 1

        dataStore = createDataStore(name = "settings")

        toggle=ActionBarDrawerToggle(this, dlDrawerLayout, R.string.open, R.string.close)
        dlDrawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        val menue: Menu = nvNavView.menu

        //-----------------1-actionViewNightMode----------------------
        val menuItemNightMode: MenuItem = menue.findItem(R.id.miNightMode)
        val actionViewSwitch: View = MenuItemCompat.getActionView(menuItemNightMode)

        var value = ""
            CoroutineScope(Dispatchers.IO).launch {
                value = read("value")
                MyApplication.currentMode = value

                if( value == "" ) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
                else if (value == "0") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    actionViewSwitch.sSwitchNightMode.isChecked = true
                }
                else if (value == "1") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }

            }


        actionViewSwitch.sSwitchNightMode.setOnClickListener {
            switchMode = 1
            MyApplication.f = 1
            lifecycleScope.launch {
                if(read("value") != "0" && read("value") != "1" ) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    save("value", "0")
                }
                else if(read("value") == "0") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    save("value", "1")
                }
                else if(read("value") == "1") {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    save("value", "0")
                }
            }
        }
        //-----------------1-actionViewNightMode----------------------

        //-----------2-actionViewCheckBoxSound--------------------
        val menuItem: MenuItem = menue.findItem(R.id.miCheckSound)
        val actionView: View = MenuItemCompat.getActionView(menuItem)

        CoroutineScope(Dispatchers.Main).launch {
            MyApplication.boxSound = read("checkSound")

            if(MyApplication.boxSound == "true") {
                actionView.cbSound.isChecked = true
            }
            else if(MyApplication.boxSound == "false") {
                actionView.cbSound.isChecked = false
            }
        }

        actionView.cbSound.setOnClickListener {
            if(actionView.cbSound.isChecked == true) {
                val mediaPlayer = MediaPlayer.create(this, R.raw.positive_beeps)
                mediaPlayer.start()
                MyApplication.boxSound = "true"
            }
            else if(actionView.cbSound.isChecked == false) {
                MyApplication.boxSound = "false"
            }
            CoroutineScope(Dispatchers.IO).launch {
                save("checkSound", MyApplication.boxSound)
            }
        }
        //-----------2-actionViewCheckBoxSound--------------------

        //------------3-actionViewAutoSave--------------------
        val menuItemAutoSafe: MenuItem = menue.findItem(R.id.miAutoSave)
        val actionViewSwitchAutoSafe: View = MenuItemCompat.getActionView(menuItemAutoSafe)

        CoroutineScope(Dispatchers.IO).launch {
            MyApplication.autoSave = read("autoSave")
            if(MyApplication.autoSave == "") {
                actionViewSwitchAutoSafe.sSwitchNightMode.isChecked = false
            }
            else if(MyApplication.autoSave == "true") {
                actionViewSwitchAutoSafe.sSwitchNightMode.isChecked = true
            }
            else if(MyApplication.autoSave == "false") {
                actionViewSwitchAutoSafe.sSwitchNightMode.isChecked = false
            }
        }
        actionViewSwitchAutoSafe.sSwitchNightMode.setOnClickListener {
            val switchState = actionViewSwitchAutoSafe.sSwitchNightMode.isChecked
            if(switchState) {
                MyApplication.autoSave = "true"
            } else {
                MyApplication.autoSave = "false"
            }

            lifecycleScope.launch {
                save("autoSave", MyApplication.autoSave)
            }
        }
        //------------3-actionViewAutoSave--------------------

        //------------4-actionViewSortItemsBy--------------------
        val menuItemSortItemsBy: MenuItem = menue.findItem(R.id.miSortItemsBy)
        val actionViewSortItemsBy: View = MenuItemCompat.getActionView(menuItemSortItemsBy)

        CoroutineScope(Dispatchers.IO).launch {
            MyApplication.sortItemsBy = read("SortItems")
        }

        var whichClickedSort = ""
        actionViewSortItemsBy.ivMore.setOnClickListener {
            CoroutineScope(Dispatchers.Main).launch {
                MyApplication.sortItemsBy = read("SortItems")
                var indexOfClicked = if(MyApplication.sortItemsBy == "m") 0 else 1
                val options = arrayOf("modification date", "creation date")
                val singleChoiceDialog = AlertDialog.Builder(this@MainActivity)
                    .setTitle("sort items by:")

                    .setSingleChoiceItems(options, indexOfClicked) { dialogInterface, i ->
                        if(i == 0) whichClickedSort = "m"
                        else whichClickedSort = "c"
                    }

                    .setPositiveButton("Ok") { _, _ ->
                        CoroutineScope(Dispatchers.Main).launch {
                            MyApplication.sortItemsBy = whichClickedSort
                            save("SortItems", whichClickedSort)
                            finish()
                            startActivity(intent)
                        }
                    }

                    .setNegativeButton("Cancel") { _, _ ->
                    }
                    .create()
                    .show()

            }
        }
        //------------4-actionViewSortItemsBy--------------------


        nvNavView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.miNightMode -> {
                    //set the switch during clicking on miNightMode item
                    if(actionViewSwitch.sSwitchNightMode.isChecked)
                        actionViewSwitch.sSwitchNightMode.isChecked = false
                    else actionViewSwitch.sSwitchNightMode.isChecked = true
                    //---------
                    MyApplication.f = 1
                    lifecycleScope.launch {
                        if(read("value") != "0" && read("value") != "1" ) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            save("value", "0")
                        }
                        else if(read("value") == "0") {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            save("value", "1")
                        }
                        else if(read("value") == "1") {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            save("value", "0")
                        }
                    }
                }

                R.id.miCheckSound -> {
                    //set the check box during clicking on the miCheckSound item
                    if(actionView.cbSound.isChecked) actionView.cbSound.isChecked = false
                    else actionView.cbSound.isChecked = true
                    //--------
                    if(actionView.cbSound.isChecked == true) {
                        val mediaPlayer = MediaPlayer.create(this, R.raw.positive_beeps)
                        mediaPlayer.start()
                        MyApplication.boxSound = "true"
                    }
                    else if(actionView.cbSound.isChecked == false) {
                        MyApplication.boxSound = "false"
                    }
                    lifecycleScope.launch {
                        save("checkSound", MyApplication.boxSound)
                    }
                }
                //------------------
                R.id.miAutoSave -> {
                    //set the switch during clicking on the item
                    if(actionViewSwitchAutoSafe.sSwitchNightMode.isChecked == true)
                        actionViewSwitchAutoSafe.sSwitchNightMode.isChecked = false
                    else actionViewSwitchAutoSafe.sSwitchNightMode.isChecked = true
                    //--------------
                    val switchState = actionViewSwitchAutoSafe.sSwitchNightMode.isChecked
                    if(switchState) {
                        MyApplication.autoSave = "true"
                    } else {
                        MyApplication.autoSave = "false"
                    }

                    lifecycleScope.launch {
                        save("autoSave", MyApplication.autoSave)
                    }
                }
                R.id.miSortItemsBy -> {
                    var isPositive = false
                    CoroutineScope(Dispatchers.Main).launch {
                        MyApplication.sortItemsBy = read("SortItems")
                        var indexOfClicked = if(MyApplication.sortItemsBy == "m") 0 else 1
                        val options = arrayOf("modification date", "creation date")
                        val singleChoiceDialog = AlertDialog.Builder(this@MainActivity)
                            .setTitle("sort items by:")

                            .setSingleChoiceItems(options, indexOfClicked) { dialogInterface, i ->
                                if(i == 0) whichClickedSort = "m"
                                else whichClickedSort = "c"
                            }

                            .setPositiveButton("Ok") { _, _ ->
                                CoroutineScope(Dispatchers.Main).launch {
                                    MyApplication.sortItemsBy = whichClickedSort
                                    save("SortItems", whichClickedSort)

                                    finish()
                                    startActivity(intent)
                                }
                            }

                            .setNegativeButton("Cancel") { _, _ ->
                            }
                            .create()
                            .show()

                    }
                }
                //----------------------------------------
                R.id.miInformation -> {
                    val intent = Intent(this, InformationActivity::class.java)
                    startActivity(intent)
                }
                //---------------open play store------------------
                R.id.miUpgrade -> {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.easytools.notespro")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.easytools.notespro")))
                    }
                }

                R.id.miOurApps -> {
                    try { //https://play.google.com/store/apps/dev?id=5336237495874752195
                        //https://play.google.com/store/apps/developer?id=Ehab+Samir
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://developer?id=Ehab+Samir")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Ehab+Samir")))
                    }
                }

                R.id.miRate -> {
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.easytools.notesfree")))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.easytools.notesfree")))
                    }
                }
                //---------------open play store------------------
                R.id.miShareApp -> {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.setType("text/plain")
                    intent.putExtra(Intent.EXTRA_TEXT, "Notes App is a great app for you notes\nTry it:\nhttps://play.google.com/store/apps/details?id=com.easytools.notesfree")
                    val chooser = Intent.createChooser(intent, null)
                    startActivity(chooser)
                }
            //----------------------------------------
            }
            true
        }

        bnv.setOnItemSelectedListener {
            when(it.itemId){
                R.id.miNotes -> {
                    replaceFragment(notesFragment)
                    MyApplication.whichFrag = 1
                }
                R.id.miTodo -> {
                    replaceFragment(todoFragment)
                    MyApplication.whichFrag = 2
                }
                R.id.miRecords -> {
                    replaceFragment(recordsFragment)
                    MyApplication.whichFrag = 3
                }
                R.id.miPhotos -> {
                    replaceFragment(photosFragment)
                    MyApplication.whichFrag = 4
                }
            }
            true
        }

    }


    /*override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            val addContactDialog = AlertDialog.Builder(this)
                .setTitle("exit")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { dialogInterface, i ->
                    finish()
                })
                .setNegativeButton("No", null)
                .create()
                .show()

            return true
        }
        else {
            return super.onKeyDown(keyCode, event)
        }
    }*/

    fun replaceFragment(fragment: Fragment)=
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragments, fragment)
            commit()
        }

    //----------------------------------------------------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    private suspend fun save(key: String, value: String) {
        val dataStoreKey = preferencesKey<String>(key)
        dataStore.edit { settings ->
            settings[dataStoreKey] = value
        }
    }
    private suspend fun read(key: String): String {
        val dataStoreKey = preferencesKey<String>(key)
        val preferences = dataStore.data.first()
        return preferences[dataStoreKey] ?: ""
    }


    override fun onPause() {
        super.onPause()
        lifecycle = 4
    }

    override fun onStart() {
        super.onStart()
        if(lifecycle == 1 && MyApplication.f != 1) {
            replaceFragment(notesFragment)
        }
        else {
            MyApplication.f = 0
        }
    }

    //-----------------------
    override fun onBackPressed() {
        super.onBackPressed()
        MyApplication.whichFrag = 0
        //for when exit from the app by clicking on back button. because in case of reoppening the app, the class MyApplication won't be called again so whichFrag will remain on the number of the last fragment that you was in it before exiting
    }

    //-------------------------------------------------------------------------------
}
