package com.easytools.notesfree.todoFragment

class TodoRepository(private val todoDao: TodoDao) {

    val allTodoOne = todoDao.getAllTodo(true)
    val allTodoTwo = todoDao.getAllTodo(false)

    suspend fun insert(todo: Todo){
        todoDao.insert(todo)
    }

    suspend fun delete(todo: Todo){
        todoDao.delete(todo)
    }

    suspend fun updateTodo(todo: Todo){
        todoDao.updateTodo(todo)
    }

}
