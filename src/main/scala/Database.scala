package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor
import scala.collection.mutable.LinkedHashMap
import android.database.CursorIndexOutOfBoundsException


object TaskTable {
  var taskTable: Option[TaskTable] = None

  def apply(context: Context): TaskTable = {
    if (taskTable.isEmpty) taskTable = Some(new TaskTable(context))
    taskTable.get
  }
}

class TaskTable(context: Context) extends SQLiteOpenHelper(context, "todo", null, 51) {
  var db: SQLiteDatabase = null

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    val q = List("drop table if exists", tableName).mkString(" ")
    db.execSQL(q)
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    val q = List("create table", tableName, "(", Task.toSQL(), ")").mkString(" ")
    db.execSQL(q)
  }

  def open() = db = getWritableDatabase()

  def insert(task: Task) = db.insert("tasks", null, task.contentValues())

  def tableName = "tasks"

  def update(task: Task): Int = {
    val whereArgs: Array[String] = Array(task.created_at.completeFormat);
    val affectedRows =
      db.update("tasks", task.contentValues(), "created_at = ?", whereArgs)
  }

  def cursor: Cursor = db.query("tasks", null, null, null, null, null, null)
}
