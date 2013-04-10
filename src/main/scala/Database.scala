package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import scala.collection.mutable.ListBuffer
import android.content.Context
import android.database.Cursor
import scala.collection.mutable.LinkedHashMap
import android.database.CursorIndexOutOfBoundsException

object DBHelper {
  var dbHelper: Option[DBHelper] = None
  def getDB(context: Context): SQLiteDatabase = dbHelper match {
    case Some(dbh) => dbh.open()
    case None => {
      dbHelper = Some(new DBHelper(context))
      dbHelper.get.open()
    }
  }
}

class DBHelper(context: Context) extends SQLiteOpenHelper(context, "todo", null, App.DbVersion) {
  var db: SQLiteDatabase = null
  val tableObjects = List[DBModel](Task, TaskList)
  implicit val c: Context = context

  override def onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = {
    // drop them tables
    tableObjects.foreach((m: DBModel) => {
      val q = m.tableDropStatement
      Log.i(q)
      db.execSQL(q)
    })

    onCreate(db)
  }

  override def onCreate(db: SQLiteDatabase) = {
    tableObjects.foreach((m: DBModel) => {
      val q = m.tableCreateStatement
      Log.i(q)
      db.execSQL(q)
    })

    // seed
    TaskList.defaultTaskLists.foreach((name: String) =>
      db.insert("task_lists", null, TaskList(name).contentValues())
    )
  }

  def open(): SQLiteDatabase = {
    db = getWritableDatabase()
    db
  }

}
