package com.easytools.notesfree.todoFragment

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
abstract class TodoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(todo: Todo)

    @Delete
    abstract suspend fun delete(todo: Todo)

    @Query("SELECT * FROM todo_table ORDER BY " +
            "CASE WHEN :byModifying = 1 THEN date END DESC, " +
            "CASE WHEN :byModifying = 0 THEN id END DESC")
    abstract fun getAllTodo(byModifying: Boolean): LiveData<List<Todo>>

    @Update
    abstract fun updateTodo(todo: Todo)

}
