package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost}
import android.widget.TabHost.TabContentFactory
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray
import android.widget.ArrayAdapter
import android.os.IBinder
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.widget.AbsListView

import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener

import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  var context: Context   = _
  var listView: ListView = _

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    setContext()
    initListView()
    initCommandButton(listView, R.id.commandButton)
    initSyncButton(listView, R.id.synchronizeButton)
    initTabs()
    adapter.showIncompleteTasks(context)

    initAddNewTaskView()
  }

  override def onDestroy() {
    TaskTable(this).close()
  }

  // Initializers

  def setContext() = context = this

  def initSyncButton(listView: ListView, id: Int) = findButton(id).setOnClickListener(onClickListener(synchronizeButtonHandler))

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
    listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

    listView.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(adapter: AdapterView[_], view: View, position: Int, arg: Long) = {
        initCommandButton(listView, R.id.commandButton)
      }
    })
  }


  def initTabs() = {
    object Tabs {
      val IncompleteTasks = "incomplete"
      val CompletedTasks = "completed"
    }

    def createTabView(context: Context, text: String): View = {
      val view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
      val tv =  view.findViewById(R.id.tabsText).asInstanceOf[TextView]
      tv.setText(text)
      view
    }

    def setupTab(tabHost: TabHost, view: View, tag: String) {
      val tabview = createTabView(tabHost.getContext(), tag)

      val setContent = tabHost.newTabSpec(tag).setIndicator(tabview).setContent(new TabContentFactory() {
          def createTabContent(tag: String): View = view
      })
      tabHost.addTab(setContent)
    }

    val tabHost = findViewById(android.R.id.tabhost).asInstanceOf[TabHost]
    tabHost.setup();
    tabHost.getTabWidget().setDividerDrawable(R.drawable.tab_divider);
    tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
      def onTabChanged(tabId: String) = tabId match {
        case Tabs.IncompleteTasks => adapter.showIncompleteTasks(context)
        case Tabs.CompletedTasks => adapter.showCompletedTasks(context)
      }
    })

    setupTab(tabHost, new TextView(this), Tabs.IncompleteTasks);
    setupTab(tabHost, new TextView(this), Tabs.CompletedTasks);
  }

  // Button handlers

  def unCheckAllItems(v: View) =
    for (i <- 0 to listView.getCount()) listView.setItemChecked(i, false)

  override def onBackPressed() = hideTaskNewForm()

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

  def synchronizeButtonHandler(view: View) = {
    Log.i("clicked synchronize!")
    val collection = Collection("http://polar-scrubland-5755.herokuapp.com", "juliusz.gonera@gmail.com", "testtest")
    collection.links.map(links => links.find(_.rel == "tasks").map(l => pr(l.href)))
  }

  def addNewTaskButtonHandler(view: View) = showNewTaskForm()

  def priorityButtonHandler(view: View) = {
    val priorities = getResources().getStringArray(R.array.task_priorities)
    val listener = (selection: String) => pr(selection)
    val dialog = new PickerDialog(this, priorities.asInstanceOf[Array[CharSequence]], listener)
    dialog.show(getSupportFragmentManager(), "priority-dialog")

  }

  def dateButtonHandler(view: View) = {
    val listener = (selection: String) => pr(selection)
    val dialog = new DatePickerDialog(this, "Date", listener)
    dialog.show(getSupportFragmentManager(), "date-dialog")
  }

  def timeButtonHandler(view: View) = {
    val listener = (hour: Int, minute: Int) => pr(hour.toString() + ":" + minute.toString())
    val dialog = new TimePickerDialog(this, listener)
    dialog.show(getSupportFragmentManager(), "time-dialog")
  }

  def repeatButtonHandler(view: View) = {
    val repeat_list = getResources().getStringArray(R.array.repeat).asInstanceOf[Array[CharSequence]]
    val listener = (selection: String) => pr(selection)
    val dialog = new PickerDialog(this, repeat_list, listener)
    dialog.show(getSupportFragmentManager(), "repeat-dialog")
  }

  // Add Task Init Code

  def initAddNewTaskView() = {
    val input = findViewById(R.id.task_title_input).asInstanceOf[TextView]
    input.setOnEditorActionListener(onEditorActionListener(handleTaskTitleInputEnterKey))
  }

  def handleTaskTitleInputEnterKey(v: TextView, actionId: Int, event: KeyEvent) = {
    def dueDate() = {
      val spinner = findSpinner(R.id.due_date)

      Date.parse(spinner.value).toString()
    }

    // def taskList = findSpinner(R.id.taskListSpinner).value

    def addNewTask() = {
      val title = findViewById(R.id.task_title_input).asInstanceOf[TextView]
      val task = new Task(title, "default")
      val spinner = findSpinner(R.id.priority)

      task.setPriority(findSpinner(R.id.priority).value)
      task.due_date = dueDate()
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
