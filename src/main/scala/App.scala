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
    var currentTaskListRestriction: TaskListRestriction = taskListRestrictions(0)

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
/*
object App {
  def showKeyboard(context: Context) = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
  }

  def hideKeyboard(context: Context, windowToken: IBinder) = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.hideSoftInputFromWindow(windowToken, 0);
  }

  def pr(context: Context, s: String) = Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

}
*/
