package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context

object Tasks {
  var tasks: ListBuffer[Task] = ListBuffer()

  def add(context: Context, task: Task) = {
    tasks += task
    val tasksTable = new TasksTable(context)
    tasksTable.insert(task)
  }

  def apply() = tasks

  def loadFromDB(context: Context) = {
    val tasksTable = new TasksTable(context)
    tasks = tasksTable.getAll()
  }

  implicit def delegateToCollection(t: Tasks.type) = tasks
}
