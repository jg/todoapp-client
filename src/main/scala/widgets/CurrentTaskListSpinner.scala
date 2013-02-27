package com.android.todoapp

import android.content.Context
import android.widget.ArrayAdapter
import android.util.AttributeSet
import android.widget.AdapterView
import android.view.View
import com.android.todoapp.Implicits._
import java.util.Calendar

object DefaultTaskLists extends Enumeration {
  val Today, Week, Inbox, Habits, Goals = Value
}

import DefaultTaskLists._
class CurrentTaskListSpinner(context: Context, attrs: AttributeSet) extends Spinner(context, attrs) {
  var currentFilter = DefaultTaskLists.Today

  fromArray(stringValues)
  setSelection(stringValues.indexOf(currentFilter))

  setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    Util.pr(context, pos.toString)
  })

  def stringValues: Array[String] = values.toArray.map(_.toString)
  // Inbox, Habits, User lists
}
