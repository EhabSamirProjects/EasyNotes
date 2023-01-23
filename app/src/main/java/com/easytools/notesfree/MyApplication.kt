package com.easytools.notesfree

import android.app.Application

class MyApplication : Application() {

    companion object{
        var whichFrag = 0
        var isSpeechToTextFabPressed = ""
        var autoSave = ""
        var sortItemsBy = ""
        var boxSound = "true"
        var currentMode = ""
        var f = 0
        var isActionModeActive = false //needed for selecting by short click after selecting by long click  and for either select or open that clicked note
    }

}