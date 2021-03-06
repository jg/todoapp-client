package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import android.widget.TabHost.TabContentFactory
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import android.os.IBinder
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.widget.AbsListView
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._
import android.database.sqlite.SQLiteDatabase

object TaskListView {
  var taskListView: Option[TaskListView] = None
  def apply(context: Context, listView: ListView, adapter: TaskAdapter): TaskListView = taskListView match {
    case Some(v) => v
    case None => {
      taskListView = Some(new TaskListView(context, listView, adapter))
      taskListView.get
    }
  }
}

class TaskListView(context: Context, listView: ListView, adapter: TaskAdapter) {
  listView.setAdapter(adapter)

  def unCheckAllItems() = {
    val views = Range(0, listView.getChildCount()).map(listView.getChildAt(_))
    views.map(_.asInstanceOf[TaskLayout].setChecked(false))
  }

  def checkedItemCount: Integer = {
    Range(0, listView.getChildCount()).count(listView.getChildAt(_).asInstanceOf[TaskLayout].isChecked())
  }

  def checkedItems: Seq[Task] = {
    val lv = listView
    Range(0, lv.getChildCount())
      .filter(lv.getChildAt(_).asInstanceOf[TaskLayout].isChecked())
      .map(adapter.getTask(_))
  }
}
