package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.android.todoapp.Utils._
import com.android.todoapp.Implicits._
import android.widget.Toast
import android.support.v4.app.FragmentActivity
import android.view.View.OnFocusChangeListener
import android.content.Intent

class TaskEditActivity extends FragmentActivity with Finders {
  var task: Task = _

  val NotSet = "Not set"

  lazy val dateSelectionDialog = {
    val listener = (selection: Date) =>
      setDueDate(Some(selection))
    new DatePickerDialog(this, "Date", listener)
  }
  lazy val timeSelectionDialog = {
    val listener = (time: Time) => setDueTime(Some(time))
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
    el.setText(if (date.isEmpty) NotSet else date.get.dateFormat)
  }

  def setDueTime(time: Option[Time]) = {
    val el = findButton(R.id.due_time)
    el.setText(if (time.isEmpty) NotSet else time.get.toString)
  }

  def initTaskEditForm() = {
    def setTaskTitle() = findEditText(R.id.task_title).setText(task.title)
    def populateTaskListSpinner() = findSpinner(R.id.task_list).fromResource(R.array.task_lists)
    def populateTaskPrioritySpinner() = {
      // populate
      val spinner = findSpinner(R.id.task_priority)
      val priorities = Priority.stringValues
      spinner.fromArray(priorities)

      // set value
      val priority = task.priority.toString
      val index = priorities.indexOf(priority)
      spinner.setSelection(index)
    }
    def populateTaskRepeatSpinner() = {
        val spinner = findSpinner(R.id.task_repeat)
        spinner.fromArray(Period.stringValues)

        for (repeat <- task.repeat) {
          val index = Period.stringValues.indexOf(repeat.toString)
          spinner.setSelection(index)
        }
    }
    def setDueDateClickHandler() = {
      findButton(R.id.due_date).setOnClickListener((v: View) =>
        dateSelectionDialog.show(getSupportFragmentManager(), "date-dialog"))
    }
    def setDueTimeClickHandler() = {
      findButton(R.id.due_time).setOnClickListener((v: View) =>
        timeSelectionDialog.show(getSupportFragmentManager(), "time-dialog"))
    }
    def setSaveButtonHandler() = {
      def taskPriority = Priority(findSpinner(R.id.task_priority).value)
      def taskTitle = findEditText(R.id.task_title).getText.toString()
      def taskList = findSpinner(R.id.task_list).value
      def taskRepeat = {
        val value = findSpinner(R.id.task_repeat).value
        if (value == NotSet)
            None
        else {
          val period = Period(value)
          Some(period)
        }
      }
      def taskDueDate = if (dateSelectionDialog.hasSelection)
        dateSelectionDialog.selection else None
      def taskDueTime = if (timeSelectionDialog.hasSelection)
        timeSelectionDialog.selection else None

      findButton(R.id.save).setOnClickListener((v: View) => {
        task.priority = taskPriority
        task.title = taskTitle
        task.task_list = taskList
        task.repeat = taskRepeat
        task.due_date = taskDueDate
        task.due_time = taskDueTime
        task.save(this)
        pr("Task updated")

        val intent = new Intent(this, classOf[MainActivity])
        startActivity(intent)
      })
    }

    populateTaskPrioritySpinner()
    populateTaskListSpinner()
    populateTaskRepeatSpinner()
    for (date <- task.due_date) dateSelectionDialog.setDate(date)
    for (time <- task.due_time) timeSelectionDialog.setInitialTime(time)

    setTaskTitle()

    setDueDate(task.due_date)
    setDueDateClickHandler()

    setDueTime(task.due_time)
    setDueTimeClickHandler()

    setSaveButtonHandler()
  }

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

}
