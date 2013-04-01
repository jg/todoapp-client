package com.android.todoapp

import android.content.ContentValues
import android.content.Context
import scala.collection.mutable.LinkedHashMap
import android.database.Cursor
import scala.collection.Map


object TaskList extends DBModel {
  val fieldMap = LinkedHashMap(
    "_id"          -> "integer primary key",
    "name"        -> "string"
  )

  def toSQL() = {
    fieldMap.map((x) => x._1 + " " + x._2).mkString(", ")
  }

  def defaultTaskLists = List("Inbox", "Habits", "Goals")


  def columnIndex(fieldName: String): Int = {
    val index = fieldMap.toIndexedSeq.indexWhere(
      (x: Tuple2[String,String]) => x._1 == fieldName
    )

    if (index != -1) index
    else throw new Exception("Field name " + fieldName + " not found in the fieldmap")
  }

  def fromCursor(cursor: Cursor): TaskList = {
    def i(s: String) = TaskList.columnIndex(s)

    val lst = scala.collection.mutable.MutableList[(String, Any)](
      ("id", cursor.getLong(0)),
      ("name", cursor.getString(1)))

    deserialize(lst.toList)
  }

  // used both in JSON and DB deserialization
  def deserialize(lst: Iterable[(String, Any)]): TaskList = {
    val taskList = new TaskList("")
    lst.foreach(el => {
      el match {
        case ("id", value: String) => taskList.id = value.toLong
        case ("id", value: Long) => taskList.id = value
        case ("name", value: String) => taskList.name = value
      }
    })
    taskList
  }

  def tableCreateStatement = "create table if not exists task_lists (" + TaskList.toSQL() + ")"

  def tableDropStatement = "drop table task_lists"
}

case class TaskList(var name: String) extends TaskListRestriction {
  var id: Long = _

  override def toString = name

  def contentValues(): ContentValues = {
    var values = new ContentValues()

    values.put("name", name)

    values
  }

}
