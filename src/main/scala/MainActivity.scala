package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import android.os.IBinder

import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._

class MainActivity extends Activity with TypedActivity with ActivityExtensions {
  var context: Context   = _
  var listView: ListView = _

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    setContext()
    initListView()
    initCommandButton(listView, R.id.commandButton)
    initTabs()
    adapter.showIncompleteTasks(context)

    initAddNewTaskView()
  }

  override def onDestroy() {
    TaskTable(this).close()
  }

  // Initializers

  def setContext() = context = this

  def initCommandButton(listView: ListView, id: Int) = {

    def initAddNewTaskButton(id: Int) = {
      val b = findButton(id)

      b.setText("+")
      b.setOnClickListener(onClickListener(addNewTaskButtonHandler))
    }

    def initMarkTasksAsCompleteButton(id: Int) = {
      val b = findButton(id)

      b.setText("✓")
      b.setOnClickListener(onClickListener(markTaskAsCompleteHandler))
    }

    def checkedItemCount(listView: ListView): Integer = {
      val checkedItems: SparseBooleanArray = listView.getCheckedItemPositions()
      Range(0, checkedItems.size()).count(i => checkedItems.valueAt(i))
    }

    if (checkedItemCount(listView) > 0)
      initMarkTasksAsCompleteButton(id)
    else
      initAddNewTaskButton(id)
  }

  def initListView() = {
    listView = findListView(R.id.taskList)

    listView.setAdapter(Tasks.adapter(context))
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    listView.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(adapter: AdapterView[_], view: View, position: Int, arg: Long) = {
        initCommandButton(listView, R.id.commandButton)
      }
    })
  }

  def initTabs() = {
    findViewById(R.id.showIncompleteTasksButton).
      setOnClickListener(onClickListener(showIncompleteTasksButtonHandler))

    findViewById(R.id.showCompletedTasksButton).
      setOnClickListener(onClickListener(showCompletedTasksButtonHandler))
  }

  // Button handlers

  def unCheckAllItems(v: View) =
    for (i <- 0 to listView.getCount()) listView.setItemChecked(i, false)

  override def onBackPressed() = hideTaskNewForm()

  def showIncompleteTasksButtonHandler(view: View) = adapter.showIncompleteTasks(context)

  def showCompletedTasksButtonHandler(view: View) = adapter.showCompletedTasks(context)

  def markTaskAsCompleteHandler(view: View) {
    def checkedItems(listView: ListView): Array[Task] = {
      val adapter = listView.getAdapter()
      val checkedItems: SparseBooleanArray = listView.getCheckedItemPositions()
      val checkedIndices = Range(0, checkedItems.size()).filter(i => checkedItems.valueAt(i))

      checkedIndices.map(i => adapter.getTask(checkedItems.keyAt(i))).toArray
    }

    def markTask(context: Context, task: Task) {
      task.markAsCompleted()
      task.save(context)
    }

    val items = checkedItems(listView).map(markTask(this,  _))
    Tasks.refresh(this)

    if (items.length == 1)
      pr("task marked as completed")
    else
      pr(items.length + " tasks marked as completed")

    unCheckAllItems(listView)
    initCommandButton(listView, R.id.commandButton)
  }

  def addNewTaskButtonHandler(view: View) = {
    showNewTaskForm()
  }

  // Add Task Init Code

  def initAddNewTaskView() = {
    initTaskPrioritySpinner(R.id.priority)
    initTaskListSpinner(R.id.task_list)
    initDueDateSpinner(R.id.due_date)

    val input = findViewById(R.id.task_title_input).asInstanceOf[TextView]
    input.setOnEditorActionListener(onEditorActionListener(handleTaskTitleInputEnterKey))
  }

  def handleTaskTitleInputEnterKey(v: TextView, actionId: Int, event: KeyEvent) = {
    def addNewTask() = {
      val task = new Task(findViewById(R.id.task_title_input).asInstanceOf[TextView])
      val spinner = findSpinner(R.id.priority)
      task.setPriority(spinner.value)
      Tasks.add(this, task)
      pr("New Task Added")
    }

    hideTaskNewForm()
    addNewTask()

    false
  }

  def hideTaskNewForm() = {
    findViewById(R.id.tasksNew).setVisibility(View.GONE)
    hideKeyboard(findEditText(R.id.task_title_input).getWindowToken())
  }

  def showNewTaskForm() = {
    findViewById(R.id.tasksNew).setVisibility(View.VISIBLE)
    val input = findEditText(R.id.task_title_input)
    input.setText("")
    input.requestFocus()
    showKeyboard()
  }

  def initTaskListSpinner(id: Int) = findSpinner(id).fromResource(R.array.task_lists)

  def initTaskPrioritySpinner(id: Int) = findSpinner(id).fromResource(R.array.task_priorities)

  def initDueDateSpinner(id: Int) = findSpinner(id).asDueDateSpinner()

  // Utility functions

  def adapter = listView.getAdapter()

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();



}
