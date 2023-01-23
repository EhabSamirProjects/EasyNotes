package com.easytools.notesfree.recordsFragment

import android.content.DialogInterface
import android.graphics.Color
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_new_record.*
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.bottom_sheet.fabBlue
import kotlinx.android.synthetic.main.bottom_sheet.fabGreen
import kotlinx.android.synthetic.main.bottom_sheet.fabGrey
import kotlinx.android.synthetic.main.bottom_sheet.fabLightRed
import kotlinx.android.synthetic.main.bottom_sheet.fabOrange
import kotlinx.android.synthetic.main.bottom_sheet.fabOrchid
import kotlinx.android.synthetic.main.bottom_sheet.fabSpringGreen
import kotlinx.android.synthetic.main.bottom_sheet.fabWhite
import kotlinx.android.synthetic.main.bottom_sheet.fabYellow
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class NewRecordActivity : AppCompatActivity() {
    var dirPath = ""
    var filename = ""
    lateinit var mediaRecorder: MediaRecorder
    private lateinit var viewModel: RecordsViewModel
    var stringTimer = ""
    var isRecording = true
    var duration = ""
    lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    var date = ""
    var itemColor = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_record)
        viewModel = ViewModelProvider(this).get(RecordsViewModel::class.java)

        bottomSheetBehavior = BottomSheetBehavior.from(clBottomSheet)
        bottomSheetBehavior.peekHeight = 0
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        vBackground.visibility = View.GONE

        startRecord()
        startTimer()

        ibSave.setOnClickListener {
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            vBackground.visibility = View.VISIBLE
            if(MyApplication.currentMode == "0") {
                clBottomSheet.setBackgroundColor(Color.parseColor("#FF000000"))
            } else
                clBottomSheet.setBackgroundColor(Color.parseColor("#FFFFFFFF"))

            etRecordName.setText(filename)
        }

        ibCancel.setOnClickListener {
            val addContactDialog = AlertDialog.Builder(this)
                .setMessage("Cancel the record?")
                .setPositiveButton(
                    "Yes",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        cancelRecord()
                    })
                .setNegativeButton("No", DialogInterface.OnClickListener { dialogInterface, i ->
                })
                .create()
                .show()

        }

        btnSaveRecord.setOnClickListener {
            var recordName = etRecordName.text.toString()
            if(recordName.isNotEmpty()) {
                filename = etRecordName.text.toString()
            }

            saveRecord()

        }

        btnCancelRecord.setOnClickListener {
            cancelRecord()
        }

        //---------------pause & resume -----------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ibPause.visibility =  View.VISIBLE
        }
        ibPause.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if(isRecording == true) {
                    mediaRecorder.pause()
                    ibPause.setImageResource(R.drawable.ic_play)
                }
                else {
                    mediaRecorder.resume()
                    ibPause.setImageResource(R.drawable.ic_pause)
                }
            }
        }
        //---------------pause & resume -----------------------------------



        //-----------------------------------------------------
        if(MyApplication.currentMode == "0") {
            fabWhite.setImageResource(R.drawable.ic_no_color_white)
        }

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
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.grey)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrange.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.orange)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabBlue.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.blue)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.green)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabYellow.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.yellow)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabLightRed.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.light_red)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrchid.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.orchid)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabSpringGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.spring_green)).substring(2)
            clBottomSheet.setBackgroundColor(Color.parseColor(itemColor))
        }

    }

    private fun startRecord(){
        dirPath = "${externalCacheDir?.absolutePath}/"
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh.mm.ss")
        date = simpleDateFormat.format(Date())
        filename = "record_$date"

        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        mediaRecorder.setOutputFile("$dirPath$date.mp3")
        mediaRecorder.prepare()
        mediaRecorder.start()

    }

    private fun saveRecord(){
        mediaRecorder.stop()
        mediaRecorder.release()

        var filePath = "$dirPath$date.mp3"
        var record = Records(filename, filePath, stringTimer, itemColor, date)
        viewModel.insert(record)

        isRecording = false
        finish()
    }

    private fun cancelRecord() {
        mediaRecorder.stop()
        File("$dirPath$date.mp3")

        isRecording = false
        finish()
    }

    private fun startTimer(){
        var seconds = 0
        var minutes = 0
        var hours = 0

        CoroutineScope(Dispatchers.Main).launch {
            while(isRecording == true) {
                delay(1000)
                seconds++
                if(seconds == 60) {
                    minutes++
                    seconds=0
                }
                if(minutes == 60) {
                    hours++
                    minutes=0
                }
                stringTimer = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
                tvTimer.text = stringTimer
            }
        }
    }

    //---------------overriding lifecycle methods ------------------------------
    //-------------------onKeyDown-----------------------------
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode == KeyEvent.KEYCODE_BACK) {

            val addContactDialog = AlertDialog.Builder(this)
                .setMessage("Save the record?")
                .setPositiveButton(
                    "Save",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        vBackground.visibility = View.VISIBLE
                        if(MyApplication.currentMode == "0") {
                            clBottomSheet.setBackgroundColor(Color.parseColor("#FF000000"))
                        } else
                            clBottomSheet.setBackgroundColor(Color.parseColor("#FFFFFFFF"))

                        etRecordName.setText(filename)
                    })
                .setNegativeButton("Don't save", DialogInterface.OnClickListener { dialogInterface, i ->
                        cancelRecord()
                    })
                .create()
                .show()

            return true
        }

        else {
            return super.onKeyDown(keyCode, event)
        }
    }
    //-------------------onKeyDown-----------------------------

    override fun onPause() {
        super.onPause()
        if(isRecording) {
            Toast.makeText(this, "recording...", Toast.LENGTH_SHORT).show()
        }
    }

    //---------------overriding lifecycles methods ------------------------------

//-----------------------------------------------------------------------------------
}


