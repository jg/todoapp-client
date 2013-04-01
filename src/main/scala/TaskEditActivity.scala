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
import android.content.Context

class TaskEditActivity extends FragmentActivity with ActivityExtensions {
  var task: Task = _

  val NotSet = "Not set"

  lazy val dateSelectionDialog = {
    val listener = (selection: Option[Date]) => selection match {
      case Some(date) => setDueDateButtonText(date.dateFormat)
      case None => setDueDateButtonText("Not Set")
    }
    new DatePickerDialog(this, "Date", listener)
  }
  lazy val timeSelectionDialog = {
    val listener = (selection: Option[Time]) => selection match {
      case Some(time) => setDueTimeButtonText(time.toString)
      case None => setDueTimeButtonText("Not Set")
    }
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
    Tasks.adapter.getTask(taskPosition)
  }

  def setDueDateButtonText(text: String) = findButton(R.id.due_date).setText(text)

  def setDueTimeButtonText(text: String) = findButton(R.id.due_time).setText(text)

  def TaskListRestrictions = app.TaskListRestrictions

  def initTaskEditForm() = {
    def setTaskTitle() = findEditText(R.id.task_title).setText(task.title)

    def populateTaskListSpinner() = {
      val spinner = findSpinner(R.id.task_list)
      val lists = TaskListRestrictions.taskLists
      spinner.fromArray(lists.toArray.map(_.toString))
      spinner.setSelection(lists.indexOf(task.task_list))
    }

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
        spinner.fromArray(RepeatPattern.stringValues)

        for (repeat <- task.repeat) spinner.setSelection(repeat.toString)
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

      def taskListId: Long = TaskListTable().findByName(findSpinner(R.id.task_list).value) match {
        case Some(taskList) => taskList.id
        case None => TaskListTable().findByName("Inbox").get.id
      }

      def taskRepeat: String = findSpinner(R.id.task_repeat).value

      def taskDueDate = if (dateSelectionDialog.hasSelection)
        dateSelectionDialog.selection else None

      def taskDueTime = if (timeSelectionDialog.hasSelection)
        timeSelectionDialog.selection else None

      findButton(R.id.save).setOnClickListener((v: View) => {
        task.priority = taskPriority
        task.title = taskTitle
        task.task_list_id = taskListId
        task.repeat = RepeatPattern(taskRepeat)
        task.due_date = taskDueDate
        task.due_time = taskDueTime
        task.save()
        pr("Task updated")

        val intent = new Intent(this, classOf[MainActivity])
        startActivity(intent)
      })
    }

    populateTaskPrioritySpinner()
    populateTaskListSpinner()
    populateTaskRepeatSpinner()
    for (due_date <- task.due_date) {
      dateSelectionDialog.setDate(due_date)
    }
    for (time <- task.due_time) {
      timeSelectionDialog.setInitialTime(time)
    }

    setTaskTitle()

    setDueDateClickHandler()

    setDueDateButtonText(
      if (task.due_date.isDefined)
        task.due_date.get.dateFormat
      else "Not Set")

    setDueTimeButtonText(
      if (task.due_time.isDefined)
        task.due_time.get.toString
      else "Not Set")

    setDueTimeClickHandler()

    setSaveButtonHandler()
  }

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

}
