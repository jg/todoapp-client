package com.android.todoapp

class Task(_title: String) {
  var body: String = ""
  def title = _title

  override def toString = title
}
