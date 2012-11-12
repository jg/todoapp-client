package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor

object Tasks {
  private[this] var adapter: Option[TaskAdapter] = None
  private[this] var taskTable: Option[TaskTable] = None

  def adapter(context: Context): TaskAdapter = adapter match {
    case Some(taskAdapter) => taskAdapter
    case None => {
      adapter = Some(new TaskAdapter(context, taskTable(context).cursor))
      adapter.get
    }
  }

  def taskTable(c: Context): TaskTable = taskTable match {
    case Some(taskTable) => taskTable
    case None => {
      taskTable = Some(new TaskTable(c))
      taskTable.get
    }
  }

  def add(c: Context, task: Task) = {
    taskTable(c).insert(task)
    adapter(c).notifyDataSetChanged()
    adapter(c).changeCursor(taskTable(c).cursor)
  }

}

class TaskAdapter(context: Context, cursor: Cursor)
  extends SimpleCursorAdapter(context,
                              R.layout.task,
                              cursor,
                              Array("title"),
                              Array(R.id.title)) {
  def getTask(i: Integer): Task = {
    val cursor: Cursor = getItem(i).asInstanceOf[Cursor]
    new Task(cursor.getString(1))
  }
}
