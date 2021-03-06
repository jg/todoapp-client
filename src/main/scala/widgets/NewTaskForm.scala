package com.android.todoapp

import android.content.res.Resources
import android.content.Context
import android.view.View
import android.support.v4.app.FragmentManager
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

import android.view.{LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener

import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._

class NewTaskForm(view: View, resources: Resources, fragmentManager: FragmentManager)(implicit context: Context with Refreshable) {
  val priorityButton = findButton(R.id.priority)
  val dateButton     = findButton(R.id.date)
  val timeButton     = findButton(R.id.time)
  val repeatButton   = findButton(R.id.repeat)

  val listener = (selection: Any) => { setButtonsHighlight() }

  lazy val prioritySelectionDialog: PickerDialog = {
    val priorities = resources.getStringArray(R.array.task_priorities)
    new PickerDialog(context, priorities.asInstanceOf[Array[CharSequence]], listener)
  }
  lazy val dateSelectionDialog = new DatePickerDialog(context, "Date", listener)
  lazy val timeSelectionDialog = new TimePickerDialog(context, listener)
  lazy val repeatSelectionDialog =
    new PickerDialog(context, RepeatPattern.stringValues.asInstanceOf[Array[CharSequence]], listener)
  lazy val selectionDialogs =
    List(prioritySelectionDialog, dateSelectionDialog, timeSelectionDialog, repeatSelectionDialog)

  def setButtonsHighlight(): Unit = {
    val bgSelectedColor = 0xFF669900

    val buttonMap = Map((priorityButton, prioritySelectionDialog),
                        (dateButton, dateSelectionDialog),
                        (timeButton, timeSelectionDialog),
                        (repeatButton, repeatSelectionDialog))

    buttonMap.foreach{case (button, dialog) => button.setPressed(dialog.hasSelection)}
  }


  // register listeners on view

  priorityButton.setOnClickListener((v: View) =>
    prioritySelectionDialog.show(fragmentManager, "priority-dialog")
  )

  dateButton.setOnClickListener((v: View) =>
   dateSelectionDialog.show(fragmentManager, "date-dialog"))

  timeButton.setOnClickListener((v: View) =>
    timeSelectionDialog.show(fragmentManager, "time-dialog"))

  repeatButton.setOnClickListener((v: View) =>
    repeatSelectionDialog.show(fragmentManager, "repeat-dialog"))

  val input = findViewById(R.id.task_title_input).asInstanceOf[TextView]
  input.setOnEditorActionListener(onEditorActionListener(handleTaskTitleInputEnterKey))

    def handleTaskTitleInputEnterKey(v: TextView, actionId: Int, event: KeyEvent) = {
    def addNewTask(title: String) = {
      val task = new Task()

      task.title.set(title)
      task.setTaskList(TaskListRestrictions.current match {
        case TaskListFilter() => TaskLists.Inbox.name
        case TaskList(name) => name
      })

      if (prioritySelectionDialog.hasSelection)
        task.priority.set(Priority(prioritySelectionDialog.selection.get))
      if (dateSelectionDialog.hasSelection)
        task.due_date.set(dateSelectionDialog.selection.get)
      if (timeSelectionDialog.hasSelection)
        task.due_time.set(timeSelectionDialog.selection.get)
      if (repeatSelectionDialog.hasSelection)
        task.repeat.set(RepeatPattern(repeatSelectionDialog.selection.get))

      task.save()
      Util.pr(context, "New Task Added")
      context.refresh()
    }

    val title = findViewById(R.id.task_title_input).asInstanceOf[TextView]
    if (title.length > 0)  {
      hide()
      addNewTask(title)
      // clear previous dialog selections
      selectionDialogs.foreach(_.clearSelection()) 
      // clear button highlight
      setButtonsHighlight()
    }

    true
  }

  def hide() = {
    findViewById(R.id.tasksNew).setVisibility(View.GONE)
    Util.hideKeyboard(context, findEditText(R.id.task_title_input).getWindowToken())
  }

  private def findViewById(id: Int) = view.findViewById(id)
  private def findEditText(id: Int) = findViewById(id).asInstanceOf[EditText]
  private def findButton(id: Int) = findViewById(id).asInstanceOf[Button]
}
