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
  lazy val taskLists = TaskLists.all

  spinner.fromArray(taskLists.map(_.toString).toArray)
  spinner.setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = taskLists(pos)
    TaskListRestrictions.setCurrent(choice)
    listener(choice)
  })

}
