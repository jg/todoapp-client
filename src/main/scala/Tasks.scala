package com.android.todoapp

import scala.collection.mutable.ListBuffer

object Tasks {
  var tasks: ListBuffer[Task] = ListBuffer()

  def add(task: Task) = {
    tasks += task
  }

  def head: Task = tasks.head
}
