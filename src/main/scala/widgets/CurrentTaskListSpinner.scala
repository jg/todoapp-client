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
  init()

  spinner.setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = taskLists(pos)
    TaskListRestrictions.setCurrent(choice)
    listener(choice)
  })

  def adapter(array: Array[String]): ArrayAdapter[String] = {
    val adapter: ArrayAdapter[String] =
      new ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    adapter
  }

  def init() =
    spinner.setAdapter(adapter(taskLists.map(_.toString)))

  def taskLists =
    TaskListRestrictions.all.toArray

}
