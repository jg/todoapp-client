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
import android.widget._
import collection.JavaConversions._
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._
import java.lang.CharSequence
import java.net.UnknownHostException
import java.util.{Timer, TimerTask}

class TaskListActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  lazy val list = findViewById(R.id.taskLists).asInstanceOf[ListView]
  lazy val taskListTable = new TaskListTable(this)
  lazy val adapter = new TaskListAdapter(this, taskListTable.cursor)
  lazy val newTaskListName = findViewById(R.id.newTaskListName).asInstanceOf[EditText]
  lazy val handler = new Handler()

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.task_list_activity)

    list.setAdapter(adapter)
    newTaskListName.setOnEditorActionListener(onEditorActionListener(handleNewTaskListNameInputEnterKey))
  }

  def handleNewTaskListNameInputEnterKey(v: TextView, actionId: Int, event: KeyEvent) = {
    val name = newTaskListName.getText().toString
    TaskLists.add(TaskList(name))
    newTaskListName.setText("")
    adapter.refresh()
    Util.pr(context, "New task list added")
    true
  }

  override def onStop() = {
    super.onStop()
  }

  def refreshViews() = {
    adapter.refresh()
  }

}
