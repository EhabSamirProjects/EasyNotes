package com.easytools.notesfree.todoFragment

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.easytools.notesfree.MyApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    val allTodo: LiveData<List<Todo>>
    val repository: TodoRepository

    init {
        val dao = TodoDatabase.createDatabaseInstance(application).getDao()

        repository = TodoRepository(dao)

        allTodo = if(MyApplication.sortItemsBy == "m") {
            repository.allTodoOne
        }
        else repository.allTodoTwo

    }

    fun insert(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(todo)
        }
    }

    fun delete(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(todo)
        }
    }

    fun updateTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTodo(todo)
        }
    }

}
