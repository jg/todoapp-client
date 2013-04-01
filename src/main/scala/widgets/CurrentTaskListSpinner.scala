package com.android.todoapp

import android.content.Context
import android.widget.ArrayAdapter
import android.util.AttributeSet
import android.widget.AdapterView
import android.view.View
import com.android.todoapp.Implicits._
import java.util.Calendar
import android.database.sqlite.SQLiteDatabase

class CurrentTaskListSpinner(spinner: Spinner, listener: (TaskListRestriction) => Any)(implicit context: Context) {
  val app = context.getApplicationContext().asInstanceOf[App]
  val TaskListRestrictions = app.TaskListRestrictions

  spinner.fromArray(TaskListRestrictions.all.map(_.toString).toArray) // Initialize spinner values

  spinner.setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = TaskListRestrictions.at(pos)
    TaskListRestrictions.setCurrent(choice)
    listener(choice)
  })

}
