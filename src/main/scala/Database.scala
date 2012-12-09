package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor
import scala.collection.mutable.LinkedHashMap
import android.database.CursorIndexOutOfBoundsException

object TaskFields {
  val fieldMap = LinkedHashMap(
    "_id"          -> "integer primary key",
    "title"        -> "text",
    "body"         -> "text",
    "completed_at" -> "date",
    "updated_at"   -> "date",
    "created_at"   -> "date",
    "due"          -> "date",
    "priority"     -> "integer"
  )

  def columnIndex(fieldName: String): Int =
    fieldMap.toIndexedSeq.indexWhere(
      (x: Tuple2[String,String]) => x._1 == fieldName
    )

  def toSQL() = fieldMap.map((x) => x._1 + " " + x._2).mkString(", ")
}


object TaskTable {
  def apply(context: Context) = {
    Log.i("TaskTable constructor invoked")
    new TaskTable(context)
  }
}

class TaskTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 9) {
  val db: SQLiteDatabase = getWritableDatabase()

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    val q = List("drop table if exists", tableName).mkString(" ")
    db.execSQL(q)
    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    val q = List("create table", tableName, "(", TaskFields.toSQL(), ")").mkString(" ")
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
