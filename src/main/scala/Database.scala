package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import scala.collection.mutable.ListBuffer
import android.content.ContentValues
import android.content.Context
import android.database.Cursor

// TODO: close the database connection, finalizer maybe?
class TaskTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 3) {
  val db: SQLiteDatabase = getWritableDatabase()

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    db.execSQL("drop table if exists tasks")
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    db.execSQL("create table tasks(_id integer primary key, title text, body text, created_at date, updated_at date, due date)")
  }

  def insert(task: Task) = {
    var values: ContentValues = new ContentValues()
    values.put("title", task.title)
    db.insert("tasks", null, values)
  }

  def cursor: Cursor = {
    db.query("tasks", null, null, null, null, null, null)
  }
}
