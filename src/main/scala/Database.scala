package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor
import scala.collection.mutable.LinkedHashMap
import android.database.CursorIndexOutOfBoundsException


object TaskTable {
  def apply(context: Context) = {
    Log.i("TaskTable constructor invoked")
    new TaskTable(context)
  }
}

class TaskTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 22) {
  val db: SQLiteDatabase = getWritableDatabase()

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    val q = List("drop table if exists", tableName).mkString(" ")
    db.execSQL(q)
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    val q = List("create table", tableName, "(", Task.toSQL(), ")").mkString(" ")
    db.execSQL(q)
  }

  def insert(task: Task) = db.insert("tasks", null, task.contentValues())

  def tableName = "tasks"

  def update(task: Task) = {
    val whereArgs: Array[String] = Array(task.id.toString());
    db.update("tasks", task.contentValues(), "_id = ?", whereArgs)
  }

  def cursor: Cursor = {
    db.query("tasks", null, null, null, null, null, null)
  }
}
