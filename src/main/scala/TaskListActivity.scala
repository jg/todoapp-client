package com.android.todoapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.{Intent, Context, SharedPreferences}
import android.os.{Bundle, Handler, IBinder}
import android.support.v4.app.{DialogFragment, FragmentActivity, FragmentManager}
import android.util.SparseBooleanArray
import android.view.{Menu, MenuItem, View, LayoutInflater, KeyEvent}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.widget.TabHost.TabContentFactory
import android.widget.{AbsListView, ArrayAdapter, Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import collection.JavaConversions._
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._
import java.lang.CharSequence
import java.net.UnknownHostException
import java.util.{Timer, TimerTask}

class TaskListActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)

    setContentView(R.layout.task_list_activity)

    val taskListTable = new TaskListTable(this)
    val list = findViewById(R.id.taskLists).asInstanceOf[ListView]
    list.setAdapter(new TaskListAdapter(this, taskListTable.cursor))
  }

  override def onStop() = {
    super.onStop()
  }

}
