package com.android.todoapp
import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import android.app.Application

import scala.collection.mutable.Queue

class App extends Application {
  object TaskListRestrictions {
    val taskListRestrictions: Queue[TaskListRestriction] = Queue(FilterToday, FilterThisWeek)
    val defaultTaskLists: Queue[TaskListRestriction] =
      Queue(TaskList("Inbox"), TaskList("Goals"), TaskList("Habits"))
    var currentTaskListRestriction: TaskListRestriction = defaultTaskLists(0)

    Log.i(currentTaskListRestriction.toString)

    def all = (taskListRestrictions ++ defaultTaskLists)

    def taskLists = defaultTaskLists

    def apply = all

    def setCurrent(taskList: TaskListRestriction) = currentTaskListRestriction = taskList

    def current = currentTaskListRestriction

    def at(i: Int) = all(i)
  }

  def taskListRestrictions = TaskListRestrictions


}

object App {
  def host = "http://polar-scrubland-5755.herokuapp.com/"
  val DbVersion = 84
}
