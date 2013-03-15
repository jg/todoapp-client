package com.android.todoapp

import android.content.Context
import android.widget.ArrayAdapter
import android.util.AttributeSet
import android.widget.AdapterView
import android.view.View
import com.android.todoapp.Implicits._
import java.util.Calendar

class CurrentTaskListSpinner(context: Context, attrs: AttributeSet) extends Spinner(context, attrs) with SelectionAccess[TaskListRestriction] {
  val adapter = Tasks.adapter(context)

  def app = context.getApplicationContext().asInstanceOf[App]

  def TaskListRestrictions = app.TaskListRestrictions

  fromArray(TaskListRestrictions.all.map(_.toString).toArray) // Initialize spinner values
  setSelection(TaskListRestrictions.current.toString) // Set starting selection

  setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = TaskListRestrictions.at(pos)
    TaskListRestrictions.setCurrent(choice)
    setSelection(choice)

    choice match {
      case FilterToday => adapter.showTasksDueToday(context)
      case FilterThisWeek => adapter.showTasksDueThisWeek()
      case TaskList(list) => adapter.showTasksInList(list)
      case _ => ()
    }
  })

}
