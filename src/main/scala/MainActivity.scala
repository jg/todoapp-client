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

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  implicit val context: Context    = this
  implicit val taskTable           = TaskTable(this)

  var taskList: TaskListView       = _
  var newTaskForm: NewTaskForm     = _
  var commandButton: CommandButton = _
  var timer: Timer                 = new Timer()
  var handler: Handler             = _

  lazy val postponePeriodSelectionDialog: PickerDialog = {
    val choices = List(TenSeconds, Hour, FourHours, SixHours, Day).map(_.toString).toArray[String]
    val listener = (selection: String) => {
      val items = taskList.checkedItems
      items.foreach((task: Task) => {
        task.setPostpone(Period(selection).get)
        task.save(context)
      })
      taskList.unCheckAllItems()
      Util.pr(context, "Postponed " + items.size + " tasks")
    }
    new PickerDialog(context, choices.asInstanceOf[Array[CharSequence]], listener)
  }

  override def onCreate(bundle: Bundle) {
    taskTable.open()

    super.onCreate(bundle)
    setContentView(R.layout.main)

    // Init widgets
    taskList = new TaskListView(context, findViewById(R.id.taskList).asInstanceOf[ListView])

    val container = findViewById(R.id.container)
    new Tabs(this, container)

    val currentTaskListSpinner = findViewById(R.id.current_task_list).asInstanceOf[CurrentTaskListSpinner]
    newTaskForm = new NewTaskForm(this, container, getResources(), getSupportFragmentManager(), currentTaskListSpinner)
    commandButton = new CommandButton(context, container, taskList, R.id.commandButton)

    val adapter = Tasks.adapter(context)
    adapter.registerCheckBoxStateChangeHandler((buttonView: CompoundButton, isChecked: Boolean) =>
      commandButton.init(R.id.commandButton))

    // sync button
    findButton(R.id.synchronizeButton).setOnClickListener((view: View) => synchronizeButtonHandler(view))

    handler = new Handler()
  }

  def setupTimer() = {
    timer = new Timer()
    val timerTask = new RestoreRepeatingPostponedTasks(this, Tasks.adapter(this))
    timer.schedule(timerTask, 1000, 1000)
  }

  class RestoreRepeatingPostponedTasks(context: Context, taskAdapter: TaskAdapter) extends TimerTask {
    def run() = {
      handler.post(new Runnable() {
        override def run() {
          implicit val c = context
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

  override def onStart() = {
    super.onStart()
    taskTable.open()
  }

  override def onStop() = {
    super.onStop()
    timer.cancel()
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
        Util.pr(this, "Manage lists")
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
