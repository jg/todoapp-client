package com.android.todoapp

class Task(_title: String) {
  var body: String = ""
  var created_at, updated_at: Long = (new java.util.Date).getTime()
  var completed: Boolean = false
  def title = _title

  override def toString = title

  def markAsCompleted() = completed = true
}
