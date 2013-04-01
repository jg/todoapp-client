package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.Cursor
import android.content.Context

object TaskTable {
  def apply()(implicit c: Context) = new TaskTable()
}

class TaskTable(implicit c: Context) {
  def db: SQLiteDatabase = DBHelper.getDB(c)

  def insert(task: Task) = db.insert("tasks", null, task.contentValues())

  def update(task: Task): Int = {
    val whereArgs: Array[String] = Array(task.created_at.completeFormat);
    db.update("tasks", task.contentValues(), "created_at = ?", whereArgs)
  }

  def cursor: Cursor = db.query("tasks", null, null, null, null, null, null)

  def all: Seq[Task] = {
    val cursor = db.query("tasks", null, null, null, null, null, null, null)
    val lst = scala.collection.mutable.ListBuffer.empty[Task]

    while (cursor.moveToNext())
      lst += Task.fromCursor(cursor)
    cursor.close()
    lst.toList
  }
}
