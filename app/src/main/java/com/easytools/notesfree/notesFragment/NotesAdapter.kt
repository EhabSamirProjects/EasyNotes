package com.easytools.notesfree.notesFragment

import android.graphics.Color.parseColor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import kotlinx.android.synthetic.main.item.view.*
import kotlinx.android.synthetic.main.item.view.tvText

class NotesAdapter(val listener: NotesInterface) : RecyclerView.Adapter<NotesAdapter.NotesViewHolder>() {
    private val allNotes = mutableListOf<Notes>()
    var previousItem: View? = null
    var selectedItems = mutableListOf<Int>()
    var longClick = false

    inner class NotesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotesViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false) //
        val viewHolder = NotesViewHolder(view)

        viewHolder.itemView.clItem.setOnClickListener {
            if(longClick && MyApplication.isActionModeActive) { //complete selecting
                //selecting and unselecting
                val position = viewHolder.adapterPosition
                if(selectedItems.contains(position)) {
                    selectedItems.remove(position)
                    viewHolder.itemView.cvNoteItem.strokeWidth = 2
                    viewHolder.itemView.cvNoteItem.setStrokeColor(parseColor("#A6A3A3")) //grey
                } else {
                    selectedItems.add(position)
                    viewHolder.itemView.cvNoteItem.strokeWidth = 6
                    viewHolder.itemView.cvNoteItem.setStrokeColor(parseColor("#FF0000")) //red
                }
                //calling the actionMode
                listener.showContextualActionMode(allNotes[position])

            } else { //open that note in activity to modify it
                val itemPosition = viewHolder.adapterPosition
                listener.updateNote(allNotes[itemPosition])
            }

        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: NotesViewHolder, position: Int) {
        var currentNote = allNotes[position]
        var itemColor = currentNote.itemColor
        holder.itemView.tvTitle.text = currentNote.title
        holder.itemView.tvText.text = currentNote.text

        if(currentNote.itemColor == "") {
            if(MyApplication.currentMode == "0") { //0 darkMode
                holder.itemView.clItem.setBackgroundColor(parseColor("#FF000000")) //black
            } else  //1 lightMode
                holder.itemView.clItem.setBackgroundColor(parseColor("#FFFFFFFF")) //white
        } else
            holder.itemView.clItem.setBackgroundColor(parseColor(currentNote.itemColor))

        holder.itemView.tvCreationDate.setText(currentNote.creationDate)
        holder.itemView.tvModifyingDate.setText(currentNote.modifyingDate)

        if(MyApplication.currentMode == "0") {
            holder.itemView.tvTitle.setTextColor(parseColor("#FFFFFFFF"))
            holder.itemView.tvText.setTextColor(parseColor("#FFFFFFFF"))
        }

        //===================================================
        holder.itemView.clItem.setOnLongClickListener {
            longClick = true
            //selecting and unselecting
            if(selectedItems.contains(position)) {
                selectedItems.remove(position)
                holder.itemView.cvNoteItem.strokeWidth = 2
                holder.itemView.cvNoteItem.setStrokeColor(parseColor("#A6A3A3")) //grey
            } else {
                selectedItems.add(position)
                holder.itemView.cvNoteItem.strokeWidth = 6
                holder.itemView.cvNoteItem.setStrokeColor(parseColor("#FF0000")) //red
            }
            //calling the actionMode
            listener.showContextualActionMode(allNotes[position])
            true
        }
        //binding the item: Was it selected or not ------------
        if(selectedItems.contains(position)) {
            holder.itemView.cvNoteItem.strokeWidth = 6
            holder.itemView.cvNoteItem.setStrokeColor(parseColor("#FF0000")) //red
        } else {
            holder.itemView.cvNoteItem.strokeWidth = 2
            holder.itemView.cvNoteItem.setStrokeColor(parseColor("#A6A3A3")) //grey
        }

        //-------------------------------------------------------------
    }

    override fun getItemCount(): Int {
        return allNotes.size
    }
    //----------
    //to update/reinitialize the entire recyclerview
    fun update(list: List<Notes>){
        allNotes.clear()
        allNotes.addAll(list)
        notifyDataSetChanged()
    }
    fun updateForRecolor(){
        selectedItems.clear()
        notifyDataSetChanged()
        Log.d("Recolor", "updateForRecolor called")
    }

}
