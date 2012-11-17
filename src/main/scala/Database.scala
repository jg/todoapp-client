package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor

// TODO: close the database connection, finalizer maybe?
class TaskTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 5) {
  val db: SQLiteDatabase = getWritableDatabase()

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    db.execSQL("drop table if exists tasks")
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    db.execSQL("create table tasks(_id integer primary key, title text, body text, completed_at date, created_at date, updated_at date, due date)")
  }

  def insert(task: Task) = {
    db.insert("tasks", null, task.contentValues())
  }

  def update(task: Task) = {
    val whereArgs: Array[String] = Array(task.id.toString());
    db.update("tasks", task.contentValues(), "_id = ?", whereArgs)
  }

  def cursor: Cursor = {
    db.query("tasks", null, null, null, null, null, null)
  }
}
