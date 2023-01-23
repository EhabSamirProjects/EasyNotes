package com.easytools.notesfree.notesFragment

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import kotlinx.android.synthetic.main.activity_new_note.*
import java.text.SimpleDateFormat
import java.util.*

class NewNoteActivity : AppCompatActivity(), RecognitionListener {
    private lateinit var viewModel: NotesViewModel
    var itemColor: String = ""
    var anotherWayToOnResume = true //autoSave
    var onStop = false  //autoSave
    var anotherWayToOnPause = false  //autoSave
    var date = "" //date
    var ModifyingDate = "" //date
    //----------------------1-speechToText---------------------
    private lateinit var speech: SpeechRecognizer
    private lateinit var recognizerIntent: Intent
    val logTag = "VoiceRecognition"
    var isSpeechRunning = "true"
    var onEndOfSpeechMethod = "false"
    var isSaved = false
    //----------------------1-speechToText---------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)
        viewModel = ViewModelProvider(this).get(NotesViewModel::class.java)


        //date
        var simpleDateFormat = SimpleDateFormat("yyyy.MM.dd_hh:mm:ss")
        date = simpleDateFormat.format(Date())

        //----------------------2-speechToText---------------------
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        speech.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        if(MyApplication.isSpeechToTextFabPressed == "true") {
            speech.startListening(recognizerIntent)
            isSpeechRunning = "true"
        }

        fabAddSpeech.setOnClickListener {
            requestRecordAudioPermission()
            if( hasRecordAudioPermission() ) {
                speech.startListening(recognizerIntent)
                isSpeechRunning = "true"
            }
        }
        //----------------------1-speechToText------------------------------------

        //-----------------------1-updateNote----------------------
        val id = intent.getIntExtra("EXTRA_ID", 0)
        val title = intent.getStringExtra("EXTRA_TITLE")
        val text = intent.getStringExtra("EXTRA_TEXT")
        val color = intent.getStringExtra("EXTRA_ITEMCOLOR")
        val oldCreationDate = intent.getStringExtra("EXTRA_CREATION_DATE")

        if(id != 0) {
            etTitle.setText(title)
            etEnterText.setText(text)
            if(color != "")
                clNewActivity.setBackgroundColor(Color.parseColor(color))
            itemColor = color!!
        }
        //-----------------------1-updateNote----------------------
        fabSaveNote.setOnClickListener {
            val titleInput = etTitle.text.toString()
            val input = etEnterText.text.toString()
            if (input.isNotEmpty() || titleInput.isNotEmpty()) {

                if(id != 0) {
                    viewModel.updateNote(Notes(id, titleInput, input, itemColor,oldCreationDate!!, date, date))
                }
                else {
                    val note =  Notes(titleInput, input,itemColor, date, "", date)
                    viewModel.insert(note)
                }
            }
            isSaved = true
            finish()
        }
        //------------------

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

            clNewActivity.setBackgroundColor(Color.parseColor(noColor))
        }
        fabGrey.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.grey)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrange.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.orange)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabBlue.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.blue)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.green)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabYellow.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.yellow)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabLightRed.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.light_red)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabOrchid.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.orchid)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }
        fabSpringGreen.setOnClickListener {
            itemColor = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.spring_green)).substring(2)
            clNewActivity.setBackgroundColor(Color.parseColor(itemColor))
        }

    }

    //-------------------onKeyDown-----------------------------
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode == KeyEvent.KEYCODE_BACK) {

            anotherWayToOnPause = true
            val titleInput = etTitle.text.toString()
            val input = etEnterText.text.toString()
            if (input.isNotEmpty() || titleInput.isNotEmpty()) {

                val id = intent.getIntExtra("EXTRA_ID", 0)
                val title = intent.getStringExtra("EXTRA_TITLE")
                val text = intent.getStringExtra("EXTRA_TEXT")
                val color = intent.getStringExtra("EXTRA_ITEMCOLOR")
                val oldCreationDate = intent.getStringExtra("EXTRA_CREATION_DATE")

                if(id == 0 || (id != 0 && (title != titleInput || text != input || color != itemColor) ) ) {
                    var message = ""
                    if(id == 0) message = "Save that item?"
                    else message = "Save the new changes?"
                    val addContactDialog = AlertDialog.Builder(this)
                        .setMessage("$message")
                        .setPositiveButton(
                            "Save",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                if(id == 0) {
                                    val note =  Notes(titleInput, input,itemColor, date, "", date)
                                    viewModel.insert(note)
                                } else {
                                    viewModel.updateNote(Notes(id, titleInput, input, itemColor,oldCreationDate!!, date, date))
                                }
                                isSaved = true
                                finish()
                            })
                        .setNegativeButton(
                            "Don't save",
                            DialogInterface.OnClickListener { dialogInterface, i ->
                                isSaved = true
                                finish()
                            })
                        .setNeutralButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->
                            dialogInterface.cancel()
                        } )
                        .create()
                        .show()
                }
                else {  // if input is not empty but text == input
                    finish()
                }

            } else { //if input is empty
                finish()
            }

            return true  // to exit from that method
        }

        else {
            return super.onKeyDown(keyCode, event)
        }
    }
    //-------------------onKeyDown-----------------------------


    //----------------------3-speechToText---------------------
    //-----------------onResume of that activity-------------------------
    override fun onResume() {
        super.onResume()
        if (MyApplication.autoSave == "true") {

            var insideVM = 0
            var currentInput = etEnterText.text.toString()
            var currentTitleInput = etTitle.text.toString()
            if (anotherWayToOnResume == false && (currentInput.isNotEmpty() || currentTitleInput.isNotEmpty()) ) {
                viewModel.allNotes.observe(this, androidx.lifecycle.Observer {
                    if (insideVM == 0) {
                        insideVM = 1
                        var lastNote = it[0]
                        viewModel.delete(lastNote)
                    }
                })
            }
        }
    }
    //-----------------onResume of that activity-------------------------

    //----------------onPause of that Activity----------------------
    override fun onPause() {
        super.onPause()
        speech.cancel()
        //-------------------------autoSave------------------------
        if (MyApplication.autoSave == "true") {

            if (anotherWayToOnPause == false) {
                anotherWayToOnResume = false
                if (MyApplication.autoSave == "true" && isSaved == false) {
                    val id = intent.getIntExtra("EXTRA_ID", 0)
                    val title = intent.getStringExtra("EXTRA_TITLE")
                    val text = intent.getStringExtra("EXTRA_TEXT")
                    val color = intent.getStringExtra("EXTRA_ITEMCOLOR")
                    val oldCreationDate = intent.getStringExtra("EXTRA_CREATION_DATE")

                    val titleInput = etTitle.text.toString()
                    val input = etEnterText.text.toString()
                    if (input.isNotEmpty() || titleInput.isNotEmpty()) {
                        if(id == 0) {
                            val note = Notes(titleInput, input, itemColor, date, "", date)
                            viewModel.insert(note)
                        } else {
                            val note = Notes(titleInput, input, itemColor, oldCreationDate!!, date, date)
                            viewModel.insert(note)
                        }
                    }
                }
            }
        }
        //-------------------------autoSave------------------------
    }
    //----------------onPause of that Activity----------------------


    override fun onStop() {
        super.onStop()
        speech.destroy()
        onStop = true
    }

    override fun onStart() {
        super.onStart()
        if (MyApplication.autoSave == "true") {

            if (onStop) {
                anotherWayToOnResume = false
            } else {
                anotherWayToOnResume = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MyApplication.isSpeechToTextFabPressed = "false"
    }
    //----------------------------

    override fun onReadyForSpeech(p0: Bundle?) {
        Toast.makeText(this, "Speak...", Toast.LENGTH_SHORT).show()
        ibSpeech.visibility = View.VISIBLE
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED)
    }

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(p0: Float) {}

    override fun onBufferReceived(p0: ByteArray?) {}

    override fun onEndOfSpeech() {}

    override fun onError(error: Int) {
        val errorMessage: String = getErrorText(error)
        Log.d(logTag, "onError: $errorMessage")
        if(error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)
            Toast.makeText(this, "Time out", Toast.LENGTH_SHORT).show()
        ibSpeech.visibility = View.GONE
    }
    private fun getErrorText(error: Int): String {
        var message = ""
        message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Didn't understand, please try again."
        }
        return message
    }

    override fun onResults(results: Bundle?) {
        val data = results!!.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if(data != null) {
            var currentActivityText = etEnterText.text.toString()
            var speechText = data.get(0)
            var allActivityText = currentActivityText + " " + speechText
            etEnterText.setText(allActivityText)
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_USER)
            speech.startListening(recognizerIntent)
        }
    }

    override fun onPartialResults(p0: Bundle?) {}

    override fun onEvent(p0: Int, p1: Bundle?) {}
    //----------------------3-speechToText---------------------

    //-----permission-------
    private fun hasRecordAudioPermission() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    private fun requestRecordAudioPermission() {
        var permissionsToRequest = mutableListOf<String>()
        if(!hasRecordAudioPermission()) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO)
        }
        if(permissionsToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 0)
        }
    }
    //-----permission-------

//-----------------------------------------------------------------------------------------------
}
