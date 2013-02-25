package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton, EditText}
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

class CommandButton(context: Context, view: View, taskList: TaskListView, id: Int) {
  init(id)

  def init(id: Int) = {
    if (taskList.checkedItemCount > 0) {
      initMarkTasksAsCompleteButton(id)
    } else {
      initAddNewTaskButton(id)
    }
  }

  private def markTask(context: Context, task: Task) {
    task.markAsCompleted()
    task.save(context)
  }

  private def initAddNewTaskButton(id: Int) = {
    val b = view.findViewById(id).asInstanceOf[Button]

    b.setText("+")
    b.setOnClickListener(this.addNewTaskButtonHandler(_: View))
  }

  private def initMarkTasksAsCompleteButton(id: Int) = {
    val b = view.findViewById(id).asInstanceOf[Button]

    b.setText("✓")
    b.setOnClickListener(this.markTaskAsCompleteHandler(_: View))
  }

  private def markTaskAsCompleteHandler(clickedView: View) {
    val items = taskList.checkedItems.map(markTask(context,  _))
    Tasks.refresh(context)

    if (items.length == 1)
      Util.pr(context, "task marked as completed")
    else
      Util.pr(context, items.length + " tasks marked as completed")

    taskList.unCheckAllItems()
    init(R.id.commandButton)
  }

  private def addNewTaskButtonHandler(clickedView: View) = {
    // Show new task form
    view.findViewById(R.id.tasksNew).setVisibility(View.VISIBLE)
    val input = view.findViewById(R.id.task_title_input).asInstanceOf[EditText]
    input.setText("")
    input.requestFocus()
    Util.showKeyboard(context)
  }

}
