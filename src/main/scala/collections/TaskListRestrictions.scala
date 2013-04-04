package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor
import android.app.ProgressDialog
import android.os.AsyncTask
import android.database.sqlite.SQLiteDatabase

object TaskListRestrictions {
  var current: TaskListRestriction = FilterToday

  def setCurrent(x: TaskListRestriction) = current = x
}
