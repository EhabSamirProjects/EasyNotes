package com.easytools.notesfree.todoFragment

import android.graphics.Color.parseColor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.easytools.notesfree.MyApplication
import com.easytools.notesfree.R
import kotlinx.android.synthetic.main.todo_item.view.*
import kotlinx.android.synthetic.main.todo_item.view.tvCreationDate
import kotlinx.android.synthetic.main.todo_item.view.tvModifyingDate
import kotlinx.android.synthetic.main.todo_item.view.tvText
import kotlinx.android.synthetic.main.todo_item.view.tvTitle

class TodoAdapter(val listener: TodoInterface) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {
    private var allTodos = mutableListOf<Todo>()
    var selectedItems = mutableListOf<Int>()
    var longClick = false

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.todo_item, parent, false )
        val viewHolder = TodoViewHolder(view)

        viewHolder.itemView.clTodoItem.setOnClickListener {
            if(longClick && MyApplication.isActionModeActive) { //complete selecting
                //selecting and unselecting
                val position = viewHolder.adapterPosition
                if(selectedItems.contains(position)) {
                    selectedItems.remove(position)
                    viewHolder.itemView.cvTodoItem.strokeWidth = 2
                    viewHolder.itemView.cvTodoItem.setStrokeColor(parseColor("#A6A3A3")) //grey
                } else {
                    selectedItems.add(position)
                    viewHolder.itemView.cvTodoItem.strokeWidth = 6
                    viewHolder.itemView.cvTodoItem.setStrokeColor(parseColor("#FF0000")) //red
                }
                //calling the actionMode
                listener.showContextualActionMode(allTodos[position])

            } else { //open that note in activity to modify it
                val itemPosition = viewHolder.adapterPosition
                listener.updateTodo(allTodos[itemPosition])
            }

        }

        viewHolder.itemView.cbCheck.setOnClickListener {
            val itemPosition = viewHolder.adapterPosition
            var isChecked = viewHolder.itemView.cbCheck.isChecked
            listener.updateCheckBox(allTodos[itemPosition], isChecked)
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val currentTodo = allTodos[position]
        holder.itemView.tvTitle.text = currentTodo.title
        holder.itemView.tvText.text = currentTodo.text
        holder.itemView.cbCheck.isChecked = currentTodo.isChecked
        //----------
        if(currentTodo.itemColor == "") {
            if(MyApplication.currentMode == "0") {
                holder.itemView.clTodoItem.setBackgroundColor(parseColor("#FF000000"))
            } else
                holder.itemView.clTodoItem.setBackgroundColor(parseColor("#FFFFFFFF"))
        } else
            holder.itemView.clTodoItem.setBackgroundColor(parseColor(currentTodo.itemColor))
        //----------
        holder.itemView.tvCreationDate.text = currentTodo.creationDate
        holder.itemView.tvModifyingDate.text = currentTodo.modifyingDate

        if(MyApplication.currentMode == "0") {
            holder.itemView.tvTitle.setTextColor(parseColor("#FFFFFFFF"))
            holder.itemView.tvText.setTextColor(parseColor("#FFFFFFFF"))
        }

        //------------------------------------
        holder.itemView.clTodoItem.setOnLongClickListener {
            longClick = true
            //selecting and unselecting
            if(selectedItems.contains(position)) {
                selectedItems.remove(position)
                holder.itemView.cvTodoItem.strokeWidth = 2
                holder.itemView.cvTodoItem.setStrokeColor(parseColor("#A6A3A3")) //grey
            } else {
                selectedItems.add(position)
                holder.itemView.cvTodoItem.strokeWidth = 6
                holder.itemView.cvTodoItem.setStrokeColor(parseColor("#FF0000")) //red
            }
            //calling the actionMode
            listener.showContextualActionMode(allTodos[position])
            true
        }
        //binding the item: Was it selected or not ------------
        if(selectedItems.contains(position)) {
            holder.itemView.cvTodoItem.strokeWidth = 6
            holder.itemView.cvTodoItem.setStrokeColor(parseColor("#FF0000")) //red
        } else {
            holder.itemView.cvTodoItem.strokeWidth = 2
            holder.itemView.cvTodoItem.setStrokeColor(parseColor("#A6A3A3")) //grey
        }

        //-------------------------------------------------------------
    }

    override fun getItemCount(): Int {
        return allTodos.size
    }
    //----------
    //to update/reinitialize the entire recyclerview
    fun update(list: List<Todo>){
        allTodos.clear()
        allTodos.addAll(list)
        notifyDataSetChanged()
    }

    fun updateForRecolor(){
        selectedItems.clear()
        notifyDataSetChanged()
        Log.d("Recolor", "updateForRecolor called")
    }

}