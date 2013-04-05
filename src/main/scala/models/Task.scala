package com.android.todoapp

import android.content.ContentValues
import android.content.Context
import scala.collection.mutable.LinkedHashMap
import android.database.Cursor
import scala.collection.Map
import android.database.sqlite.SQLiteDatabase


object Task extends DBModel {
  val fieldMap = LinkedHashMap(
    "_id"          -> "integer primary key",
    "title"        -> "string",
    "completed_at" -> "string",
    "updated_at"   -> "string",
    "created_at"   -> "string",
    "due_date"     -> "string",
    "due_time"     -> "integer",
    "priority"     -> "integer",
    "repeat"       -> "string",
    "postpone"     -> "string",
    "task_list_id" -> "integer"
  )

  def columnIndex(fieldName: String): Int = {
    val index = Task.fieldMap.toIndexedSeq.indexWhere(
      (x: Tuple2[String,String]) => x._1 == fieldName
    )

    if (index != -1) index
    else throw new Exception("Field name " + fieldName + " not found in the fieldmap")
  }

  def toSQL() = {
    val fields = fieldMap.map((x) => x._1 + " " + x._2).mkString(", ")
    fields + ", FOREIGN KEY(task_list_id) REFERENCES task_list(id)"

  }

  def tableCreateStatement = "create table if not exists tasks (" + Task.toSQL() + ")"

  def tableDropStatement = "drop table tasks"

  def fromDataList(lst: List[Data]): Task = {
    val title = lst.find(el => el.name == "title").get.value.get
    new Task(title)
  }

  def deserialize(lst: Iterable[(String, Any)])(implicit context: Context): Task = (new Task("")).deserialize(lst)

  def fromCursor(cursor: Cursor)(implicit context: Context): Task = (new Task("")).fromCursor(cursor)
}

class Task(var title: String) {
  var id: Long                      = -1
  var task_list_id: Long            = 1
  var completed_at: Option[Date]    = None
  var created_at: Date              = Date.now
  var updated_at: Date              = Date.now
  var due_date: Option[Date]        = None
  var due_time: Option[Time]        = None
  var priority: Priority            = new Priority(Priority.Normal)
  var repeat: Option[RepeatPattern] = None
  var postpone: Option[Period]      = None

  def task_list(implicit context: Context): String = TaskListTable(context).findById(task_list_id) match {
    case Some(lst) => lst.name
    case None => throw new Exception("TaskList with id " + task_list_id + " not found")
  }

  def setTaskList(name: String)(implicit context: Context) = {
    task_list_id = TaskListTable(context).findByName(name) match {
      case Some(taskList) => taskList.id
      case None => {
        val taskList = TaskList(name)
        TaskListTable(context).insert(taskList)
        taskList.id
      }
    }
  }

  def fieldMap(implicit context: Context) = Map(
    ("_id", id),
    ("title", title),
    ("completed_at", completed_at),
    ("created_at", created_at),
    ("updated_at", updated_at),
    ("due_date", due_date),
    ("due_time", due_time),
    ("task_list_id", task_list_id),
    ("priority", priority),
    ("repeat", repeat),
    ("postpone", postpone)
  )

  override def toString = title

  def markAsCompleted() = completed_at = Some(Date.today)

  def contentValues(): ContentValues = {
    var values = new ContentValues()

    values.put("title", title)
    values.put("task_list_id", task_list_id: java.lang.Long)
    values.put("created_at", created_at.completeFormat)
    values.put("updated_at", updated_at.completeFormat)
    values.put("priority", priority.serialize: java.lang.Integer)

    if (due_date.isDefined)
      values.put("due_date", due_date.get.completeFormat)
    else
      values.putNull("due_date")

    if (due_time.isDefined)
      values.put("due_time", due_time.get.toInt: Integer)
    else
      values.putNull("due_time")

    if (repeat.isDefined)
      values.put("repeat", repeat.get.toString)
    else
      values.putNull("repeat")

    if (completed_at.isDefined)
      values.put("completed_at", completed_at.get.completeFormat)
    else
      values.putNull("completed_at")

    if (postpone.isDefined)
      values.put("postpone", postpone.get.toString)
    else
      values.putNull("postpone")


    values
  }

  def save()(implicit context: Context) {
    updated_at = Date.now
    if ( savedP() )
      TaskTable().update(this)
    else
      id = TaskTable().insert(this)
  }

  def savedP(): Boolean = id != -1

  def isCompleted = !completed_at.isEmpty

  // TODO: extract this into separate class
  def toJSON(expectedParams: List[String])(implicit context: Context) = {
    def jsonObject(name: String, contents: String) = "{\"" + name + "\": {" + contents + "}" + "}"

    def isExpected(param: String): Boolean = !expectedParams.find(_ == param).isEmpty

    def stringJSONValue(key: String, value: String) = "\"" + key + "\":" + "\"" + value + "\""

    def intJSONValue(key: String, value: Int) = "\"" + key + "\":" + value.toString

    def longJSONValue(key: String, value: Long) = "\"" + key + "\":" + value.toString

    def jsonKeyValue(key: String, value: Any) = {
      value match {
        case value: Int => intJSONValue(key, value)
        case value: java.lang.Long => longJSONValue(key, value)
        case value: Date => stringJSONValue(key, value.completeFormat)
        case value: Time => intJSONValue(key, value.toInt)
        case value @ _ => stringJSONValue(key, value.toString)
      }
    }
    val matchingKeyValues = if (expectedParams.isEmpty) fieldMap else fieldMap.filter(x => isExpected(x._1))

    val keyValues = matchingKeyValues.map(kv => kv._2 match {
      // case None => stringJSONValue(kv._1, "nil")
      case None => stringJSONValue(kv._1, "nil")
      case Some(content) => jsonKeyValue(kv._1, content)
      case content => jsonKeyValue(kv._1, content)
    })

    jsonObject("task", keyValues.mkString(","))
  }

  // used both in JSON and DB deserialization
  def deserialize(lst: Iterable[(String, Any)])(implicit context: Context): Task = {
    lst.foreach(el => {
      el match {
        case ("id", value: String) => id = value.toLong
        case ("id", value: Long) => id = value
        case ("id", value: Int) => id = value
        case ("task_list_id", value: Long) => {

          TaskListTable(context).findById(value) match {
            case Some(taskList) => {
              task_list_id = value
            }
            case None => {
              TaskListTable(context).findByName("Inbox").get.id
            }
          }
        }
        case ("task_list", name: String) => {
          task_list_id = TaskListTable(context).findByName(name) match {
            case Some(taskList) => taskList.id
            case None => {
              val taskList = TaskList(name)
              TaskListTable(context).insert(taskList)
              taskList.id
            }
          }
        }
        case ("updated_at", value: String) => updated_at = Date(value)
        case ("created_at", value: String) => created_at = Date(value)
        case ("completed_at", value: String) => completed_at = Some(Date(value))
        case ("due_time", value: Int) => due_time = Some(Time.fromMinutes(value))
        case ("due_time", value: String) => due_time = Some(Time.fromMinutes(value.toInt))
        case ("due_date", value: String) => due_date = Some(Date(value))
        case ("repeat", value: String) => repeat = RepeatPattern(value)
        case ("priority", value: String) => priority = Priority(value)
        case ("priority", value: Integer) => priority = Priority.deserialize(value)
        case ("title", value: String) => title = value
        case ("postpone", value: String) => postpone = Period(value)
      }
    })
    this
  }

  def columnIndex(fieldName: String)(implicit context: Context): Int = {
    val index = Task.fieldMap.toIndexedSeq.indexWhere(
      (x: Tuple2[String,Any]) => x._1 == fieldName
    )

    if (index != -1) index
    else throw new Exception("Field name " + fieldName + " not found in the fieldmap")
  }

  def fromCursor(cursor: Cursor)(implicit context: Context): Task = {
    def i(s: String) = columnIndex(s)

    def isPresent(field: String) = !cursor.isNull(i(field))

    val lst = scala.collection.mutable.MutableList[(String, Any)](
      ("id", cursor.getInt(i("_id"))),
      ("updated_at", cursor.getString(i("updated_at"))),
      ("created_at", cursor.getString(i("created_at"))),
      ("priority", cursor.getInt(i("priority"))),
      ("title", cursor.getString(i("title"))))

    if (isPresent("completed_at")) lst += (("completed_at", cursor.getString(i("completed_at"))))
    if (isPresent("due_date"))     lst += (("due_date", cursor.getString(i("due_date"))))
    if (isPresent("due_time"))     lst += (("due_time", cursor.getInt(i("due_time"))))
    if (isPresent("repeat"))       lst += (("repeat", cursor.getString(i("repeat"))))
    if (isPresent("task_list_id"))    lst += (("task_list_id", cursor.getLong(i("task_list_id"))))
    if (isPresent("postpone"))     lst += (("postpone", cursor.getString(i("postpone"))))

    // Log.i(lst.toList.mkString(", "))

    deserialize(lst.toList)
  }

  def isReadyToRepeat: Boolean = {
    if (completed_at.isDefined) {
      repeat match {
        case Some(RepeatAfter(period)) =>
          Date.now.secondDifference(completed_at.get) > period.amount
        case repeatPattern @ Some(RepeatEvery(period)) =>
          repeatPattern.get.asInstanceOf[RepeatEvery].isNextPeriod(completed_at.get, Date.now)
        case _ => false
      }
    } else false
  }

  def repeatTask() = {
    completed_at = None
    updated_at = Date.now
  }

  def setPostpone(period: Period) = {
    if (completed_at.isEmpty) {
      postpone = Some(period)
      updated_at = Date.now
    }
  }

  def resetPostpone() = {
    postpone = None
    updated_at = Date.now
  }

  def isPostponeOver: Boolean = postpone match {
    case Some(period) => Date.now.secondDifference(updated_at) > period.amount
    case _ => false
  }
}
