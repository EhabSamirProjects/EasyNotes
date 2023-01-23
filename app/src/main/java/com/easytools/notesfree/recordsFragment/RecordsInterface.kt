package com.easytools.notesfree.recordsFragment

interface RecordsInterface{

    fun playRecord(record: Records)

    fun updateRecord(record: Records)

    fun deleteRecord(selectedRecords: List<Records>)

    fun showContextualActionMode(record: Records)

}
