package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor
import android.app.ProgressDialog
import android.os.AsyncTask
import android.database.sqlite.SQLiteDatabase

object TaskListRestrictions {
  private[this] var currentRestriction: Option[TaskListRestriction] = None
  val prefName = "currentTaskListRestriction"

  def current(implicit context: Context): TaskListRestriction = currentRestriction match {
    case Some(r) => r
    case None => FilterToday
  }

  def setCurrent(x: TaskListRestriction)(implicit c: Context) = {
    PreferenceStorage(c).put(prefName, x.toString)
    currentRestriction = Some(x)
  }

  def setCurrentFromPrefs()(implicit c: Context): Unit =
    for (name <- PreferenceStorage(c).get(prefName); tlr <- withName(name)) setCurrent(tlr)

  def all(implicit c: Context) = List(FilterToday, FilterThisWeek) ++ TaskLists.all

  def withName(name: String)(implicit c: Context): Option[TaskListRestriction] = all.find(_.toString == name)
}
