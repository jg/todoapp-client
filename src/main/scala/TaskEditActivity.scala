package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.android.todoapp.Utils._
import com.android.todoapp.Implicits._
import android.widget.Toast
import android.support.v4.app.FragmentActivity
import android.view.View.OnFocusChangeListener

class TaskEditActivity extends FragmentActivity with Finders {
  var task: Task = _
  lazy val dateSelectionDialog = {
    val listener = (selection: String) =>
      setDueDate(Some(Date(selection)))
    new DatePickerDialog(this, "Date", listener)
  }
  lazy val timeSelectionDialog = {
    val listener = (hour: Int, minutes: Int) =>
      setDueTime(Some(Time.fromMinutes(hour*60+minutes)))
    new TimePickerDialog(this, listener)
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.task_edit)

    task = getTaskFromIntent()

    initTaskEditForm()
  }

  def getTaskFromIntent() = {
    val intent = getIntent()
    val taskPosition = intent.getIntExtra("taskPosition", -1)
    val adapter = Tasks.adapter(this)
    adapter.getTask(taskPosition)
  }

  def setDueDate(date: Option[Date]) = {
    val el = findButton(R.id.due_date)
    el.setText(
      if (date.isEmpty)
        "Not set"
      else
        date.get.dateFormat
    )
  }

  def setDueTime(time: Option[Time]) = {
    val el = findButton(R.id.due_time)
    el.setText(
      if (time.isEmpty)
        "Not set"
      else
        time.get.toString
    )
  }

  def initTaskEditForm() = {
    def setTaskTitle() = findEditText(R.id.task_title).setText(task.title)
    def setTaskPriority() = {
      val priority = task.priority.toString
      val priorities = getResources().getStringArray(R.array.task_priorities)
      val index = priorities.indexOf(priority)
      findSpinner(R.id.task_priority).setSelection(index)
    }
    def setTaskRepeat() = {
      findSpinner(R.id.task_repeat).setSelection(
        if (task.repeat.isEmpty)
          0
        else {
          val repeat_periods = getResources().getStringArray(R.array.task_repeat)
          repeat_periods.indexOf(task.repeat.get)
      })
    }
    def populateTaskListSpinner() = findSpinner(R.id.task_list).fromResource(R.array.task_lists)
    def populateTaskPrioritySpinner() = findSpinner(R.id.task_priority).fromResource(R.array.task_priorities)
    def populateTaskRepeatSpinner() = findSpinner(R.id.task_repeat).fromResource(R.array.task_repeat)
    def setDueDateClickHandler() = {
      findButton(R.id.due_date).setOnClickListener((v: View) =>
        dateSelectionDialog.show(getSupportFragmentManager(), "date-dialog"))
    }
    def setDueTimeClickHandler() = {
      findButton(R.id.due_time).setOnClickListener((v: View) =>
        timeSelectionDialog.show(getSupportFragmentManager(), "time-dialog"))
    }

    populateTaskPrioritySpinner()
    populateTaskListSpinner()
    populateTaskRepeatSpinner()

    setTaskTitle()
    setTaskPriority()

    setDueDate(task.due_date)
    setDueDateClickHandler()

    setDueTime(task.due_time)
    setDueTimeClickHandler()
  }

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

}
