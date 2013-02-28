package com.android.todoapp

import android.content.ContentValues
import android.content.Context
import scala.collection.mutable.LinkedHashMap
import android.database.Cursor

object Task {
  val fieldMap = LinkedHashMap(
    "_id"          -> "integer primary key",
    "title"        -> "string",
    "completed_at" -> "integer",
    "updated_at"   -> "integer",
    "created_at"   -> "integer",
    "due_date"     -> "integer",
    "due_time"     -> "integer",
    "task_list"    -> "string",
    "priority"     -> "string",
    "repeat"       -> "string"
  )

  def columnIndex(fieldName: String): Int = {
    val index = fieldMap.toIndexedSeq.indexWhere(
      (x: Tuple2[String,String]) => x._1 == fieldName
    )

    if (index != -1)
      index
    else
      throw new Exception("Field name " + fieldName + " not found in the fieldmap")
  }

  def toSQL() = fieldMap.map((x) => x._1 + " " + x._2).mkString(", ")

  def fromDataList(lst: List[Data]): Task = {
    val title = lst.find(el => el.name == "title").get.value.get
    new Task(title)
  }

  def fromCursor(cursor: Cursor): Task = {
    def i(s: String) = columnIndex(s)

    def isPresent(field: String) = !cursor.isNull(i(field))

    val title = cursor.getString(columnIndex("title"))
    val task = new Task(title)

    task.id           = cursor.getInt(columnIndex("_id"))
    task.updated_at   = Date.fromMillis(cursor.getLong(columnIndex("updated_at")))
    task.created_at   = Date.fromMillis(cursor.getLong(columnIndex("created_at")))
    if (isPresent("completed_at")) task.completed_at = Some(Date.fromMillis(cursor.getLong(i("completed_at"))))
    if (isPresent("due_date")) task.due_date = Some(Date(cursor.getString(i("due_date"))))
    if (isPresent("due_time")) task.due_time = Some(Time.fromMinutes(cursor.getInt(i("due_time"))))
    if (isPresent("repeat")) task.repeat = Some(Period(cursor.getString(columnIndex("repeat"))))
    task.priority = Priority(cursor.getString(columnIndex("priority")))
    if (isPresent("task_list")) task.task_list = cursor.getString(columnIndex("task_list"))

    task
  }
}

class Task(var title: String) {
  var id: Long = -1
  var completed_at: Option[Date] = None

  var created_at = Date.now
  var updated_at = Date.now

  var due_date: Option[Date] = None
  var due_time: Option[Time] = None
  var priority: Priority = new Priority(Priority.Normal)
  var repeat: Option[Period] = None
  var task_list: String = "master"

  override def toString = title

  def markAsCompleted() = completed_at = Some(Date.today)

  def contentValues(): ContentValues = {
    var values = new ContentValues()
    values.put("title", title)
    values.put("task_list", task_list)
    values.put("created_at", created_at)
    values.put("updated_at", updated_at)
    if (!due_date.isEmpty) values.put("due_date", due_date.get.toString)
    if (!due_time.isEmpty) values.put("due_time", due_time.get.toInt: Integer)
    if (!repeat.isEmpty) values.put("repeat", repeat.get.toString)
    values.put("priority", priority.toString)
    completed_at match {
      case Some(date) => values.put("completed_at", date)
      case None => values.putNull("completed_at")
    }

    values
  }

  def save(context: Context) {
    if ( savedP() )
      Tasks.update(context, this)
    else
      Tasks.add(context, this)
  }

  def savedP(): Boolean = id != -1

  def isCompleted = !completed_at.isEmpty

  def toJSON = {
  }

}
