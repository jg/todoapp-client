package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor
import android.app.ProgressDialog
import android.os.AsyncTask
import android.database.sqlite.SQLiteDatabase

object TaskLists {
  def all(implicit context: Context): Seq[TaskList] = table.all

  def find(taskList: TaskList)(implicit c: Context): Option[TaskList] =
    all.find(_.id == taskList.id)

  def remove(taskList: TaskList)(implicit c: Context): Boolean =
    table.remove(taskList.id) == 1

  def add(taskList: TaskList)(implicit c: Context) =
    table.insert(taskList)

  private def table(implicit c: Context): TaskListTable = TaskListTable(c)
}
