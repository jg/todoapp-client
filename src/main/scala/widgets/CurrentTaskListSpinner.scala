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
  // OnItemSelectedListener fires when CurrentTaskListSpinner is first initialized
  var count = 0

  init()

  spinner.setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = taskLists(pos)
    listener(choice)
    // Do not overwrite the current task list on widget initialization
    if (count > 0) TaskListRestrictions.setCurrent(choice)
    count = count + 1
  })

  def adapter(array: Array[String]): ArrayAdapter[String] = {
    val adapter: ArrayAdapter[String] =
      new ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    adapter
  }

  def taskLists =
    TaskListRestrictions.all.toArray

  def init() = {
    spinner.setAdapter(adapter(taskLists.map(_.toString)))
    TaskListRestrictions.setCurrentFromPrefs()
    spinner.setSelection(TaskListRestrictions.currentIndex)
  }

}
