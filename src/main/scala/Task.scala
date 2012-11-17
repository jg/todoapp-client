package com.android.todoapp

import android.content.ContentValues
import android.content.Context

class Task(_title: String) {
  var id: Long = -1
  var body: String = ""
  var created_at, updated_at: Long = (new java.util.Date).getTime()
  var completed_at: Option[Long] = None
  def title = _title

  override def toString = title

  def markAsCompleted() = completed_at = Some((new java.util.Date).getTime())

  def contentValues(): ContentValues = {
    var values = new ContentValues()
    values.put("title", title)
    values.put("body", body)
    values.put("created_at", created_at.asInstanceOf[Double])
    values.put("updated_at", updated_at.asInstanceOf[Double])
    completed_at match {
      case Some(stuff) => values.put("completed_at", stuff.asInstanceOf[Double])
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

}
