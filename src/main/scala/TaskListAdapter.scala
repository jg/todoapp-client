package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
import android.content.Context
import android.widget.{TextView, CheckBox, FilterQueryProvider, CursorAdapter, CompoundButton}
import android.database.Cursor
import android.view.{ViewGroup, View, LayoutInflater}
import android.app.Activity
import com.android.todoapp.Implicits._
import android.graphics.Color
import com.android.todoapp.Utils._
import android.widget.Toast
import android.graphics.Paint
import android.os.Handler

class TaskListAdapter(context: Context, cursor: Cursor) extends CursorAdapter(context, cursor, true) {
  implicit val c: Context = context
  val taskListTable = new TaskListTable(context)

  override def bindView(view: View, context: Context, cursor: Cursor) {
    val taskList = TaskList.fromCursor(cursor)
    val taskListName = view.findViewById(R.id.taskListName).asInstanceOf[TextView]
    val deleteIcon = view.findViewById(R.id.deleteTaskList)

    taskListName.setText(taskList.name)
    deleteIcon.setOnClickListener((v: View) => {
      // move all tasks from tasklist to Inbox
      TaskLists.find(taskList).get.tasks.foreach((_: Task).setTaskList("Inbox"))
      // remove task list
      TaskLists.remove(taskList)
      notifyDataSetChanged()
    })
  }

  override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
    val inflater = LayoutInflater.from(context)
    val v = inflater.inflate(R.layout.task_list, parent, false)
    bindView(v, context, cursor)

    v
  }

}
