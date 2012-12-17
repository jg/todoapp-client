package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor

object Tasks {
  private[this] var adapter: Option[TaskAdapter] = None

  def adapter(context: Context): TaskAdapter = adapter match {
    case Some(taskAdapter) => taskAdapter
    case None => {
      adapter = Some(new TaskAdapter(context, TaskTable(context).cursor))
      adapter.get
    }
  }

  def add(c: Context, task: Task) = {
    task.id = TaskTable(c).insert(task)
    refresh(c)
  }

  def update(c: Context, task: Task) = {
    TaskTable(c).update(task)
    refresh(c)
  }

  def refresh(c: Context) {
    adapter(c).notifyDataSetChanged()
    adapter(c).getFilter().filter("")
  }

}
