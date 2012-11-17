package com.android.todoapp

import android.content.Context
import android.widget.{SimpleCursorAdapter}
import android.widget.{FilterQueryProvider}
import android.database.Cursor

class TaskAdapter(context: Context, cursor: Cursor)
  extends SimpleCursorAdapter(context,
                              R.layout.task,
                              cursor,
                              Array("title"),
                              Array(R.id.title)) {


  def showIncompleteTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is null", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def showCompletedTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is not null", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def getTask(i: Integer): Task = {
    val cursor: Cursor = getItem(i).asInstanceOf[Cursor]
    val task = new Task(cursor.getString(1))

    task.id = cursor.getInt(0)
    task.body = cursor.getString(2)
    task.completed_at = Some(cursor.getLong(3).asInstanceOf[Long])
    task.created_at = cursor.getLong(4).asInstanceOf[Long]
    task.updated_at = cursor.getLong(5).asInstanceOf[Long]

    task
  }
}
