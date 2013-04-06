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
  import PropertyConversions._
  lazy val task: Task = {
    val intent = getIntent()
    val taskId = intent.getIntExtra("taskId", -1)
    Tasks.findById(taskId) match {
      case Some(task) => task
      case None => throw new Exception("No task with taskId: " + taskId)
    }
  }
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

    initTaskEditForm()
  }

  def setDueDateButtonText(text: String) = findButton(R.id.due_date).setText(text)

  def setDueTimeButtonText(text: String) = findButton(R.id.due_time).setText(text)

  def initTaskEditForm() = {
    def setTaskTitle() = findEditText(R.id.task_title).setText(task.title.get)

    def populateTaskListSpinner() = {
      val spinner = findSpinner(R.id.task_list)
      val lists = TaskLists.all.toArray.map(_.toString)
      spinner.fromArray(lists)
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

      def taskListId: Long = TaskListTable(this).findByName(findSpinner(R.id.task_list).value) match {
        case Some(taskList) => taskList.id
        case None => TaskListTable(this).findByName("Inbox").get.id
      }

      def taskRepeat: String = findSpinner(R.id.task_repeat).value

      def taskDueDate = if (dateSelectionDialog.hasSelection)
        dateSelectionDialog.selection else None

      def taskDueTime = if (timeSelectionDialog.hasSelection)
        timeSelectionDialog.selection else None

      findButton(R.id.save).setOnClickListener((v: View) => {
        task.priority.set(taskPriority)
        task.title.set(taskTitle)
        task.task_list_id.set(taskListId)
        task.repeat.setOpt(RepeatPattern(taskRepeat))
        task.due_date.setOpt(taskDueDate)
        task.due_time.setOpt(taskDueTime)
        task.save()
        pr("Task updated")

        val intent = new Intent(this, classOf[MainActivity])
        startActivity(intent)
      })
    }

    def populateForm() = {
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
    }

    populateForm()
    setSaveButtonHandler()
  }

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

}
