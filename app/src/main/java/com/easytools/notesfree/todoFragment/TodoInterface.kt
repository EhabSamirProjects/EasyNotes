package com.easytools.notesfree.todoFragment

interface TodoInterface{

    fun deleteTodo(selectedNotes: List<Todo>)

    fun updateCheckBox(todo: Todo, isChecked: Boolean)

    fun updateTodo(todo: Todo)

    fun showContextualActionMode(todo: Todo)

}
