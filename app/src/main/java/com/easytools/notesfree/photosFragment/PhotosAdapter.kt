package com.easytools.notesfree.photosFragment

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import kotlinx.android.synthetic.main.photo_item.view.*

class PhotosAdapter(val listener: PhotosInterface) : RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder>() {
    var allPhotos = mutableListOf<Photos>()
    var selectedItems = mutableListOf<Int>()
    var longClick = false

    inner class PhotosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotosViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.photo_item, parent, false)
        val viewHolder = PhotosViewHolder(view)

        viewHolder.itemView.clPhotoItem.setOnClickListener {
            if(longClick && MyApplication.isActionModeActive) { //complete selecting
                //selecting and unselecting
                val position = viewHolder.adapterPosition
                if(selectedItems.contains(position)) {
                    selectedItems.remove(position)
                    viewHolder.itemView.cvPhotoItem.strokeWidth = 2
                    viewHolder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#A6A3A3")) //grey
                } else {
                    selectedItems.add(position)
                    viewHolder.itemView.cvPhotoItem.strokeWidth = 6
                    viewHolder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#FF0000")) //red
                }
                //calling the actionMode
                listener.showContextualActionMode(allPhotos[position])

            } else { //open that note in activity to modify it
                val itemPosition = viewHolder.adapterPosition
                listener.fullScreen(allPhotos[itemPosition].bitmap)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: PhotosViewHolder, position: Int) {
        var currentPhoto = allPhotos[position].bitmap
        holder.itemView.ivPhoto.setImageBitmap(currentPhoto)

        if(MyApplication.currentMode == "0") {
            holder.itemView.clPhotoItem.setBackgroundColor(Color.parseColor("#FF000000"))
        }

        //===================================================
        holder.itemView.clPhotoItem.setOnLongClickListener {
            longClick = true
            //selecting and unselecting
            if(selectedItems.contains(position)) {
                selectedItems.remove(position)
                holder.itemView.cvPhotoItem.strokeWidth = 2
                holder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#A6A3A3")) //grey
            } else {
                selectedItems.add(position)
                holder.itemView.cvPhotoItem.strokeWidth = 6
                holder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#FF0000")) //red
            }
            //calling the actionMode
            listener.showContextualActionMode(allPhotos[position])
            true
        }
        //binding the item: Was it selected or not ------------
        if(selectedItems.contains(position)) {
            holder.itemView.cvPhotoItem.strokeWidth = 6
            holder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#FF0000")) //red
        } else {
            holder.itemView.cvPhotoItem.strokeWidth = 2
            holder.itemView.cvPhotoItem.setStrokeColor(Color.parseColor("#A6A3A3")) //grey
        }

        //-------------------------------------------------------------
    }

    override fun getItemCount(): Int {
        return allPhotos.size
    }
    //----------
    //to update/reinitialize the entire recyclerview
    fun update(list: List<Photos>){
        allPhotos.clear()
        allPhotos.addAll(list)
        notifyDataSetChanged()
    }
    fun updateForRecolor(){
        selectedItems.clear()
        notifyDataSetChanged()
        Log.d("Recolor", "updateForRecolor called")
    }

}
