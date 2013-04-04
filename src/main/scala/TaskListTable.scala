package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.database.Cursor

object TaskListTable {
  def apply(context: Context) = new TaskListTable(context)
}

class TaskListTable(context: Context) {
  val TableName = "task_lists"

  def adapter: TaskAdapter = TaskAdapter(context, cursor)

  def insert(taskList: TaskList) = db.insert(TableName, null, taskList.contentValues())

  def cursor: Cursor = db.query(TableName, null, null, null, null, null, null)

  def remove(id: Long) = {
    db.delete(TableName, "id = ?", Array[String](id.toString))
  }

  def all: Seq[TaskList] = {
    val lst = scala.collection.mutable.ListBuffer.empty[TaskList]
    val cursor = db.query(TableName, null, null, null, null, null, null)

    while (cursor.moveToNext())
      lst += TaskList.fromCursor(cursor)
    cursor.close()
    lst.toIndexedSeq
  }

  def db: SQLiteDatabase = DBHelper.getDB(context)

  def findByName(name: String): Option[TaskList] = {
    all.find((taskList: TaskList) => taskList.name == name)
  }

  def findById(id: Long): Option[TaskList] = all.find((_:TaskList).id == id)
}
