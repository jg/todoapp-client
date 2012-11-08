package com.android.todoapp

class Task(_title: String) {
  var body: String = ""
  var created_at, updated_at: Long = (new java.util.Date).getTime()
  def title = _title

  override def toString = title
}
