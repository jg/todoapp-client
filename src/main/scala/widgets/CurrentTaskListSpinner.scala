package com.android.todoapp

import android.content.Context
import android.widget.ArrayAdapter
import android.util.AttributeSet
import android.widget.AdapterView
import android.view.View
import com.android.todoapp.Implicits._
import java.util.Calendar
import scala.collection.mutable.Queue

abstract class TaskListRestriction

case class TaskList(name: String) extends TaskListRestriction {
  override def toString = name
}

case class TaskListFilter extends TaskListRestriction
case object FilterToday extends TaskListFilter {
  override def toString = "Today"
}
case object FilterThisWeek extends TaskListFilter {
  override def toString = "Week"
}

class CurrentTaskListSpinner(context: Context, attrs: AttributeSet) extends Spinner(context, attrs) with SelectionAccess[TaskListRestriction] {
  val adapter = Tasks.adapter(context)
  val defaultTaskLists = List(FilterToday, FilterThisWeek, TaskList("Inbox"), TaskList("Goals"), TaskList("Habits")) : List[TaskListRestriction]
  val taskLists = Queue[TaskListRestriction]() ++= defaultTaskLists
  var currentFilter = taskLists(1)

  fromArray(taskLists.toArray.map(_.toString))
  setSelection(currentFilter)

  setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
    val choice = taskLists(pos)
    // Util.pr(context, stringValues(pos))
    choice match {
      case FilterToday => adapter.showTasksDueToday(context)
      case FilterThisWeek => adapter.showTasksDueThisWeek()
      case TaskList(list) => adapter.showTasksInList(list)
      case _ => ()
    }
  })
}
