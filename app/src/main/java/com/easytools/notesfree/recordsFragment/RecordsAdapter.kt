package com.easytools.notesfree.recordsFragment

import android.graphics.Color.parseColor
import android.media.MediaPlayer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.recyclerview.widget.RecyclerView
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import kotlinx.android.synthetic.main.record_item.view.*
import kotlinx.coroutines.*
import java.util.*

class RecordsAdapter(val listener: RecordsInterface) : RecyclerView.Adapter<RecordsAdapter.RecordsViewHolder>(){
    private val allRecords = mutableListOf<Records>()
    var mediaPlayer = MediaPlayer()
    var previousFilePath = ""
    var previousItem: View? = null
    var job = CoroutineScope(Dispatchers.Main).launch {  }
    var selectedItems = mutableListOf<Int>()
    var longClick = false

    inner class RecordsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.record_item, parent, false)
        val viewHolder = RecordsViewHolder(view)

        var currentItem = viewHolder.itemView
        currentItem.ibPlay.setOnClickListener {
            job.cancel()

            val itemPosition = viewHolder.adapterPosition

            var clickedRecord = allRecords[itemPosition]

            if(clickedRecord.filePath != previousFilePath) {

                previousItem?.ibPlay?.setImageResource(R.drawable.ic_play)
                mediaPlayer.reset()
                mediaPlayer.setDataSource(clickedRecord.filePath)
                mediaPlayer.prepare()
                mediaPlayer.start()
                currentItem.ibPlay.setImageResource(R.drawable.ic_pause)

                //---
                previousFilePath = clickedRecord.filePath

            }
            else {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                    currentItem.ibPlay.setImageResource(R.drawable.ic_play)
                }
                else {
                    mediaPlayer.start()
                    currentItem.ibPlay.setImageResource(R.drawable.ic_pause)
                }
            }

            //-----
            currentItem.sb.max = mediaPlayer.duration

            job = CoroutineScope(Dispatchers.Main).launch {
                var seconds = 0
                var minutes = 0
                var hours = 0
                while (mediaPlayer.isPlaying) {
                    delay(1000L)
                    currentItem.sb.progress = mediaPlayer.currentPosition
                    //----
                    seconds = mediaPlayer.currentPosition.div(1000).toInt()
                    if(seconds > 59) {
                        minutes = seconds / 60
                        seconds = seconds % 60
                    }
                    if(minutes > 59) {
                        hours = minutes / 60
                        minutes = minutes % 60
                    }

                    var stringTimer = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
                    currentItem.tvCurrentTime.text = stringTimer

                }
                //when exiting from that loop  this means that mediaPlayer.isPlaying = false  so that:
                currentItem.ibPlay.setImageResource(R.drawable.ic_play)
            }

            previousItem = currentItem
        }

        viewHolder.itemView.sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                    if(p2) {
                        mediaPlayer.seekTo(p1)
                        var seconds = 0
                        var minutes = 0
                        var hours = 0

                        seconds = p1.div(1000)
                        if(seconds > 59) {
                            minutes = seconds / 60
                            seconds = seconds % 60
                        }
                        if(minutes > 59) {
                            hours = minutes / 60
                            minutes = minutes % 60
                        }
                        var stringTimer = String.format(Locale.getDefault(),"%02d:%02d:%02d", hours, minutes, seconds)
                        currentItem.tvCurrentTime.text = stringTimer
                    }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {}

            override fun onStopTrackingTouch(p0: SeekBar?) {}

        })

        viewHolder.itemView.clRecordItem.setOnClickListener {
            if(longClick && MyApplication.isActionModeActive) { //complete selecting
                //selecting and unselecting
                val position = viewHolder.adapterPosition
                if(selectedItems.contains(position)) {
                    selectedItems.remove(position)
                    viewHolder.itemView.cvRecordItem.strokeWidth = 2
                    viewHolder.itemView.cvRecordItem.setStrokeColor(parseColor("#A6A3A3")) //grey
                } else {
                    selectedItems.add(position)
                    viewHolder.itemView.cvRecordItem.strokeWidth = 6
                    viewHolder.itemView.cvRecordItem.setStrokeColor(parseColor("#FF0000")) //red
                }
                //calling the actionMode
                listener.showContextualActionMode(allRecords[position])

            } else { //open that note in activity to modify it
                val itemPosition = viewHolder.adapterPosition
                listener.updateRecord(allRecords[itemPosition])
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        var currentRecord = allRecords[position]
        holder.itemView.tvFilename.text = currentRecord.filename
        holder.itemView.tvDuration.text = currentRecord.duration

        if(currentRecord.itemColor == "") {
            if(MyApplication.currentMode == "0") {
                holder.itemView.clRecordItem.setBackgroundColor(parseColor("#FF000000"))
            } else
                holder.itemView.clRecordItem.setBackgroundColor(parseColor("#FFFFFFFF"))
        } else
            holder.itemView.clRecordItem.setBackgroundColor(parseColor(currentRecord.itemColor))

        holder.itemView.tvDate.text = currentRecord.date

        if(MyApplication.currentMode == "0") {
            holder.itemView.tvFilename.setTextColor(parseColor("#FFFFFFFF"))
        }

        //===================================================
        holder.itemView.clRecordItem.setOnLongClickListener {
            longClick = true
            //selecting and unselecting
            if(selectedItems.contains(position)) {
                selectedItems.remove(position)
                holder.itemView.cvRecordItem.strokeWidth = 2
                holder.itemView.cvRecordItem.setStrokeColor(parseColor("#A6A3A3")) //grey
            } else {
                selectedItems.add(position)
                holder.itemView.cvRecordItem.strokeWidth = 6
                holder.itemView.cvRecordItem.setStrokeColor(parseColor("#FF0000")) //red
            }
            //calling the actionMode
            listener.showContextualActionMode(allRecords[position])
            true
        }
        //binding the item: Was it selected or not ------------
        if(selectedItems.contains(position)) {
            holder.itemView.cvRecordItem.strokeWidth = 6
            holder.itemView.cvRecordItem.setStrokeColor(parseColor("#FF0000")) //red
        } else {
            holder.itemView.cvRecordItem.strokeWidth = 2
            holder.itemView.cvRecordItem.setStrokeColor(parseColor("#A6A3A3")) //grey
        }

        //-------------------------------------------------------------
    }

    override fun getItemCount(): Int {
        return allRecords.size
    }
    //----------
    //to update/reinitialize the entire recyclerview
    fun update(list: List<Records>){
        allRecords.clear()
        allRecords.addAll(list)
        notifyDataSetChanged()
    }
    fun updateForRecolor(){
        selectedItems.clear()
        notifyDataSetChanged()
        Log.d("Recolor", "updateForRecolor called")
    }

}
