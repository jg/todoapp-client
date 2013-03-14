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

class NewTaskForm(context: Context, view: View, resources: Resources, fragmentManager: FragmentManager, currentTaskListSpinner: CurrentTaskListSpinner) {

  // buttons

  val priorityButton = findButton(R.id.priority)
  val dateButton     = findButton(R.id.date)
  val timeButton     = findButton(R.id.time)
  val repeatButton   = findButton(R.id.repeat)

  val listener = (selection: Any) => { highlightButtonsWithSelection() }

  lazy val prioritySelectionDialog: PickerDialog = {
    val priorities = resources.getStringArray(R.array.task_priorities)
    new PickerDialog(context, priorities.asInstanceOf[Array[CharSequence]], listener)
  }

  lazy val dateSelectionDialog = {
    new DatePickerDialog(context, "Date", listener)
  }

  lazy val timeSelectionDialog = {
    new TimePickerDialog(context, listener)
  }

  lazy val repeatSelectionDialog = {
    val repeat_list = resources.getStringArray(R.array.task_repeat).asInstanceOf[Array[CharSequence]]
    new PickerDialog(context, repeat_list, listener)
  }

  lazy val selectionDialogs = List(prioritySelectionDialog, dateSelectionDialog, timeSelectionDialog, repeatSelectionDialog)

  def highlightButtonsWithSelection(): Unit = {
    val bgSelectedColor = 0xFF669900

    val buttonMap = Map((priorityButton, prioritySelectionDialog),
                        (dateButton, dateSelectionDialog),
                        (timeButton, timeSelectionDialog),
                        (repeatButton, repeatSelectionDialog))

    buttonMap.foreach{case (button, dialog) => {
      if (dialog.hasSelection) button.setPressed(true)
    }}
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
      val task = new Task(title)

      task.task_list = currentTaskListSpinner.selection.get match {
        case FilterToday => "Inbox"
        case FilterThisWeek => "Inbox"
        case TaskList(list) => list
      }
      if (prioritySelectionDialog.hasSelection)
        task.priority = Priority(prioritySelectionDialog.selection.get)
      if (dateSelectionDialog.hasSelection)
        task.due_date = Some(dateSelectionDialog.selection.get)
      if (timeSelectionDialog.hasSelection)
        task.due_time = Some(timeSelectionDialog.selection.get)
      if (repeatSelectionDialog.hasSelection)
        task.repeat   = Some(Period(repeatSelectionDialog.selection.get))

      Tasks.add(context, task)
      pr("New Task Added")
    }

    val title = findViewById(R.id.task_title_input).asInstanceOf[TextView]
    if (title.length > 0)  {
      hide()
      addNewTask(title)
      selectionDialogs.foreach(_.clearSelection()) // clear previous dialog selections
    }

    true
  }

  def hide() = {
    findViewById(R.id.tasksNew).setVisibility(View.GONE)
    hideKeyboard(findEditText(R.id.task_title_input).getWindowToken())
  }

  private def findViewById(id: Int) = view.findViewById(id)
  private def findEditText(id: Int) = findViewById(id).asInstanceOf[EditText]
  private def findButton(id: Int) = findViewById(id).asInstanceOf[Button]
  private def pr(s: String) = Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

  def showKeyboard() = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
  }

  def hideKeyboard(windowToken: IBinder) = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.hideSoftInputFromWindow(windowToken, 0);
  }

}
