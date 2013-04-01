package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.database.Cursor

object TaskListTable {
  def apply()(implicit context: Context) = new TaskListTable()
}

class TaskListTable(implicit context: Context) {
  val TableName = "task_lists"

  def adapter: TaskAdapter = TaskAdapter(context, cursor)

  def insert(taskList: TaskList) = db.insert(TableName, null, taskList.contentValues())

  def cursor: Cursor = db.query(TableName, null, null, null, null, null, null)

  def all: IndexedSeq[TaskList] = {
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
