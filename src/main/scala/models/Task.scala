package com.android.todoapp

import android.content.ContentValues
import android.content.Context
import scala.collection.mutable.LinkedHashMap

object Task {
  val fieldMap = LinkedHashMap(
    "_id"          -> "integer primary key",
    "title"        -> "string",
    "completed_at" -> "string",
    "updated_at"   -> "string",
    "created_at"   -> "string",
    "due_date"     -> "string",
    "task_list"    -> "string",
    "priority"     -> "integer"
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

}

class Task(_title: String) {
  var id: Long = -1
  var created_at, due_date, updated_at: String = Date.today.toString()
  var task_list: String = ""
  var completed_at: Option[String] = None
  var priority: Int = 0
  def title = _title

  override def toString = title

  def markAsCompleted() = completed_at = Some(Date.today.toString())

  def contentValues(): ContentValues = {
    var values = new ContentValues()
    values.put("title", title)
    values.put("task_list", task_list)
    values.put("created_at", created_at)
    values.put("updated_at", updated_at)
    values.put("due_date", due_date)
    values.put("priority", priority: Integer)
    completed_at match {
      case Some(date) => values.put("completed_at", date)
      case None => values.putNull("completed_at")
    }

    values
  }

  def save(context: Context) {
    if ( savedP() ) {
      Tasks.update(context, this)
    } else {
      Tasks.add(context, this)
    }
  }

  def savedP(): Boolean = {
    id != -1
  }

  def setPriority(str: String) = {
    this.priority = str match {
      case "high" => 1
      case "low" => -1
      case _ => 0
    }
  }

}
