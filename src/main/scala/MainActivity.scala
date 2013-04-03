package com.android.todoapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.{Intent, Context, SharedPreferences}
import android.os.{Bundle, Handler, IBinder}
import android.support.v4.app.{DialogFragment, FragmentActivity, FragmentManager}
import android.util.SparseBooleanArray
import android.view.{Menu, MenuItem, View, LayoutInflater, KeyEvent}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.widget.TabHost.TabContentFactory
import android.widget.{AbsListView, ArrayAdapter, Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import collection.JavaConversions._
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._
import java.lang.CharSequence
import java.net.UnknownHostException
import java.util.{Timer, TimerTask}
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  var newTaskForm: NewTaskForm     = _
  var commandButton: CommandButton = _
  var timer: Timer                 = new Timer()
  var handler: Handler             = _
  var taskList: TaskListView = null
  var adapter: TaskAdapter = null

  lazy val postponePeriodSelectionDialog: PickerDialog = {
    val choices = List(TenSeconds, Hour, FourHours, SixHours, Day).map(_.toString).toArray[String]
    val listener = (selection: String) => {
      val items = taskList.checkedItems
      items.foreach((task: Task) => {
        task.setPostpone(Period(selection).get)
        task.save()
      })
      taskList.unCheckAllItems()
      Util.pr(context, "Postponed " + items.size + " tasks")
    }
    new PickerDialog(context, choices.asInstanceOf[Array[CharSequence]], listener)
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)
    val container = findViewById(R.id.container)
    val app: App = getApplicationContext().asInstanceOf[App]
    handler = new Handler()

    // Init TaskAdapter
    adapter = new TaskAdapter(this, DBHelper.getDB(this).rawQuery("select * from tasks", null))

    adapter.registerTaskClickHandler((taskCursorPosition: Int) =>  {
      val intent = new Intent(context, classOf[TaskEditActivity])
      intent.putExtra("taskPosition", taskCursorPosition);
      context.startActivity(intent)
    })

    // Init widgets

    // CurrentTaskListSpinner
    val taskListChangeListener = (choice: TaskListRestriction) => {
      choice match {
        case FilterToday => adapter.showTasksDueToday()
        case FilterThisWeek => adapter.showTasksDueThisWeek()
        case TaskList(list) => adapter.showTasksInList(list)
        case _ => ()
      }
      refresh()
    }
    new CurrentTaskListSpinner(findViewById(R.id.current_task_list).asInstanceOf[Spinner], taskListChangeListener)

    // TaskListView
    taskList = new TaskListView(this, findViewById(R.id.taskList).asInstanceOf[ListView], adapter)

    // Tabs
    val tabListener = (tab: Tab) => {
      tab match {
        case incompleteTasksTab() => adapter.showIncompleteTasks()
        case completedTasksTab() => adapter.showCompletedTasks()
      }
      refresh()
    }
    new Tabs(container, tabListener)

    // NewTaskForm
    newTaskForm = new NewTaskForm(container, getResources(), getSupportFragmentManager(), app.TaskListRestrictions.current)

    // CommandButton
    commandButton = new CommandButton(container, taskList, R.id.commandButton)
    Tasks.adapter.registerCheckBoxStateChangeHandler((buttonView: CompoundButton, isChecked: Boolean) =>
      commandButton.init(R.id.commandButton))

    // SyncButton
    findButton(R.id.synchronizeButton).setOnClickListener((view: View) => synchronizeButtonHandler(view))
  }

  def initCurrentTaskListSpinner() = {
    val app: App = getApplicationContext().asInstanceOf[App]
    val taskListSpinner = findViewById(R.id.current_task_list).asInstanceOf[Spinner]
    val TaskListRestrictions = app.TaskListRestrictions

    taskListSpinner.fromArray(TaskListRestrictions.all.map(_.toString).toArray) // Initialize spinner values

    taskListSpinner.setOnItemSelectedListener((parent: AdapterView[_], view: View, pos: Int, id: Long) => {
      val choice = TaskListRestrictions.at(pos)
      TaskListRestrictions.setCurrent(choice)

      choice match {
        case FilterToday => adapter.showTasksDueToday()
        case FilterThisWeek => adapter.showTasksDueThisWeek()
        case TaskList(list) => adapter.showTasksInList(list)
        case _ => ()
      }
      refresh()
    })
  }

  def refresh() = {
    adapter.notifyDataSetChanged()
    // taskList.setAdapter(adapter)
  }

  def setupTimer() = {
    timer = new Timer()
    val timerTask = new RestoreRepeatingPostponedTasks(this, Tasks.adapter)
    timer.schedule(timerTask, 1000, 1000)
  }


  class RestoreRepeatingPostponedTasks(context: Context, taskAdapter: TaskAdapter) extends TimerTask {
    def run() = {
      handler.post(new Runnable() {
        override def run() {
          Tasks.restorePostponed()
          Tasks.restoreRepeating()
        }
      })
    }
  }

  override def onBackPressed() = newTaskForm.hide()

  override def onResume() = {
    super.onResume()
    setupTimer()
  }

  override def onPause() = {
    super.onPause()
    timer.cancel()
  }

  override def onStop() = {
    super.onStop()
    timer.cancel()
    DBHelper.getDB(this).close()
  }

  def initSyncButton(listView: ListView, id: Int) = findButton(id).setOnClickListener(onClickListener(synchronizeButtonHandler))

  def synchronizeButtonHandler(view: View): Unit = {
    val listener = (c: Credentials) => {
      if (Tasks.checkCredentials(c)) {
        Tasks.synchronize(c)
      }
    }

    // show dialog if credentials not already present
    Credentials.get(this) match {
      case Some(credentials) => {
        Log.i("Using existing credentials " + credentials.toString)
        listener(credentials)
      }
      case None => {
        val dialog = new LoginDialog(this, listener)
        dialog.show(getSupportFragmentManager(), "login-dialog")
      }
    }
  }

  override def onCreateOptionsMenu(menu: Menu): Boolean = {
    val inflater = getMenuInflater();
    inflater.inflate(R.menu.menu, menu);
    true
  }

  override def onOptionsItemSelected(item: MenuItem): Boolean = {
    item.getItemId() match {
      case R.id.lists => {
        startActivity(new Intent(this, classOf[TaskListActivity]))
        true
      }
      case R.id.postpone => {
        if (taskList.checkedItemCount > 0)
          postponePeriodSelectionDialog.show(getSupportFragmentManager(), "postpone-selection")
        else Util.pr(context, "No tasks selected")
        true
      }
      case _ => {
        Util.pr(this, "Default")
        true
      }
    }
  }
}
