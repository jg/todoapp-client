package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor
import scala.collection.immutable.TreeMap
import android.database.CursorIndexOutOfBoundsException

object TaskFields {
  val fieldMap = TreeMap(
    "_id"          -> "integer primary key",
    "title"        -> "text",
    "body"         -> "text",
    "completed_at" -> "date",
    "created_at"   -> "date",
    "due"          -> "date",
    "priority"     -> "integer"
  )

  def columnIndex(fieldName: String): Int = fieldMap.toIndexedSeq.indexWhere(
        (x: Tuple2[String,String]) => x._1 == fieldName)

  def getFromCursor(cursor: Cursor, fieldName: String): Option[Any] = {
    def fieldType(s: String) = s.split(" ").head

    def getValue(f: (Int => Any)): Option[Any] = {
      try {
        val i: Int = columnIndex(fieldName)

        if (i == -1) return None;
        Some(f(i))
      } catch {
        case e: CursorIndexOutOfBoundsException => None
      }
    }


    fieldType(fieldMap(fieldName)) match {
      case "integer" => getValue(cursor.getInt)
      case "text"    => getValue(cursor.getString)
      case "date"    => getValue(cursor.getDouble)
      case _         => None
    }
  }

  def toSQL() = fieldMap.map((x) => x._1 + " " + x._2).mkString(", ")
}

// TODO: close the database connection, finalizer maybe?
class TaskTable(context: Context) extends SQLiteOpenHelper(context, "tasks", null, 6) {
  val db: SQLiteDatabase = getWritableDatabase()
  val tableName = "tasks"

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

  def update(task: Task) = {
    val whereArgs: Array[String] = Array(task.id.toString());
    db.update("tasks", task.contentValues(), "_id = ?", whereArgs)
  }

  def cursor: Cursor = {
    db.query("tasks", null, null, null, null, null, null)
  }


}
