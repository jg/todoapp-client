package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import scala.collection.mutable.ListBuffer
import android.content.ContentValues
import android.content.Context

class TasksTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 2) {
  val db: SQLiteDatabase = getWritableDatabase()

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    db.execSQL("drop table if exists tasks")
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    db.execSQL("create table tasks(id integer primary key, title text, body text, created_at date, updated_at date, due date)")
  }


  def insert(task: Task) = {
    var values: ContentValues = new ContentValues()
    values.put("title", task.title)
    db.insert("tasks", null, values)
    close()
  }

  def getAll(): ListBuffer[Task] = {
    val cursor = db.query("tasks", null, null, null, null, null, null)
    val result = new ListBuffer[Task]()
    while (cursor.moveToNext()) {
      result += new Task(cursor.getString(1))
    }

    close()
    result
  }
}
