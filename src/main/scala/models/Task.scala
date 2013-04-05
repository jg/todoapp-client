package com.android.todoapp

import android.content.ContentValues
import android.content.Context
import scala.collection.mutable.LinkedHashMap
import android.database.Cursor
import scala.collection.Map
import android.database.sqlite.SQLiteDatabase

// Generalize into a PropertySet?
trait TaskProperties {
  import TypeSerializers._
  var id: Property[Long]              = Property[Long]("id", None)
  var title: Property[String]         = Property[String]("title", None)
  var task_list_id: Property[Long]    = Property[Long]("task_list_id", Some(1))
  var completed_at: Property[Date]    = Property[Date]("completed_at", None)
  var created_at: Property[Date]      = Property[Date]("created_at", Some(Date.now))
  var updated_at: Property[Date]      = Property[Date]("updated_at", Some(Date.now))
  var due_date: Property[Date]        = Property[Date]("due_date", None)
  var due_time: Property[Time]        = Property[Time]("due_time", None)
  var priority: Property[Priority]    = Property[Priority]("priority", Some(new Priority(Priority.Normal)))
  var repeat: Property[RepeatPattern] = Property[RepeatPattern]("repeat", None)
  var postpone: Property[Period]      = Property[Period]("postpone", None)
  val properties: List[Property[_]]   = List(id, title, task_list_id, completed_at, created_at, updated_at, due_date, due_time, priority, repeat, postpone)

  def toSQL() = {
    properties.map((p: Property[_]) =>
      p.name + " " + p.sqlType).mkString(", ") + ", FOREIGN KEY(task_list_id) REFERENCES task_list(id)"
  }

  def contentValues(): ContentValues = {
    val values = new ContentValues()
    properties.foreach((p: Property[_]) => { p.addToContentValues(values) })
    values
  }

  def fromCursor(cursor: Cursor)(implicit context: Context): Task = {
    val task = new Task()
    task.properties.foreach((p: Property[_]) =>
      p.setFromCursor(cursor, propertyIndex(p)))
    task
  }

  private def propertyIndex(p: Property[_]) = properties.indexOf(p)

  // construct a task from key value pairs, used in fromJSON
  def deserialize(lst: Iterable[(String, Any)])(implicit context: Context): Task = {
    val task = new Task()
    lst.foreach(_ match {
      case (propertyName, value) => task.propertyWithName(propertyName).setFromAny(value)
      case _ => throw new Exception("Something is horribly wrong here...")
    })

    task
  }

  def propertyWithName(name: String): Property[_] = {
    properties.find(_.name == name) match {
      case Some(p) => p
      case None => throw new Exception("No such property: " + name)
    }
  }

  // TODO: extract this into separate class
  def toJSON(expectedParams: List[String])(implicit context: Context) = {
    def jsonObject(name: String, contents: String) = "{\"" + name + "\": {" + contents + "}" + "}"

    def isExpected(param: String): Boolean = !expectedParams.find(_ == param).isEmpty

    val matchingKeyValues: Traversable[Property[_]] = 
      if (expectedParams.isEmpty)
        properties
      else
        properties.filter((p: Property[_]) => isExpected(p.name))

    val keyValues = matchingKeyValues.map(_.jsonKeyValue)

    jsonObject("task", keyValues.mkString(","))
  }
}

object Task extends DBModel with TaskProperties {
  def tableCreateStatement = "create table if not exists tasks (" + Task.toSQL() + ")"

  def tableDropStatement = "drop table tasks"
}

class Task(implicit context: Context) extends TaskProperties {
  def task_list(implicit context: Context): String = TaskListTable(context).findById(task_list_id.get) match {
    case Some(lst) => lst.name
    case None => throw new Exception("TaskList with id " + task_list_id + " not found")
  }

  def setTaskList(name: String)(implicit context: Context) = {
    task_list_id.set(TaskListTable(context).findByName(name) match {
      case Some(taskList) => taskList.id
      case None => {
        val taskList = TaskList(name)
        TaskListTable(context).insert(taskList)
        taskList.id
      }
    })
  }

  override def toString = title.get

  def markAsCompleted() = completed_at.set(Date.today)

  def save()(implicit context: Context) {
    updated_at.set(Date.now)
    if ( savedP() )
      TaskTable().update(this)
    else
      id.set(TaskTable().insert(this))
  }

  def savedP(): Boolean = id != -1

  def isCompleted = !completed_at.isEmpty

  def isReadyToRepeat: Boolean = {
    if (completed_at.isDefined) {
      repeat.value match {
        case Some(RepeatAfter(period)) =>
          Date.now.secondDifference(completed_at.get) > period.amount
        case repeatPattern @ Some(RepeatEvery(period)) =>
          repeatPattern.get.asInstanceOf[RepeatEvery].isNextPeriod(completed_at.get, Date.now)
        case _ => false
      }
    } else false
  }

  def repeatTask() = {
    completed_at.reset()
    updated_at.set(Date.now)
  }

  def setPostpone(period: Period) = {
    if (completed_at.isEmpty) {
      postpone.set(period)
      updated_at.set(Date.now)
    }
  }

  def resetPostpone() = {
    postpone.reset()
    updated_at.set(Date.now)
  }

  def isPostponeOver: Boolean = postpone.value match {
    case Some(period) => Date.now.secondDifference(updated_at.get) > period.amount
    case _ => false
  }
}
