package com.android.todoapp

case class TaskList(name: String) extends TaskListRestriction {
  override def toString = name
}
