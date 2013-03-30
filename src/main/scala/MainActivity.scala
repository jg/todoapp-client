package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater, KeyEvent}
import android.widget.{Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
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
import java.net.UnknownHostException
import java.util.{Timer, TimerTask}
import android.app.ProgressDialog
import android.os.Handler

import android.content.SharedPreferences

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  implicit val context: Context   = this
  var taskList: TaskListView = _

  var newTaskForm: NewTaskForm = _
  var commandButton: CommandButton = _
  implicit val taskTable = TaskTable(this)
  var timer: Timer = new Timer()
  var handler: Handler = _

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
    new PostponeButton(context, findButton(R.id.postponeButton),  getSupportFragmentManager(), taskList)

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
    def restorePostponedTasks(tasks: Seq[Task]) = { // restore postponed tasks that are ready
      val readyTasks = tasks.filter((t: Task) => t.isPostponeOver)
      val readyCount = readyTasks.size
      readyTasks.foreach((t: Task) => {
        t.resetPostpone()
        t.save(context)
      })

      if (readyCount > 0)
        Util.pr(context, "Restored " + readyCount.toString + " tasks from postponed state")
    }

    def restoreRepeatingTasks(tasks: Seq[Task]) = { // restore repeating tasks that are ready
      val readyTasks = tasks.filter((t: Task) => t.isReadyToRepeat)
      val readyCount = readyTasks.size
      readyTasks.foreach((t: Task) => {
        t.repeatTask()
        Log.i(t.toJSON(List()))
        t.save(context)
      })

      if (readyCount > 0)
        Util.pr(context, "Restored " + readyCount.toString + " recurring tasks from completed state")
    }

    def run() = {
      handler.post(new Runnable() {
        override def run() {
          val tasks = taskAdapter.allTasks

          restorePostponedTasks(tasks)
          restoreRepeatingTasks(tasks)
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
      if (!Credentials.isCorrect(c, this)) {
        Util.pr(this, "Credentials not correct, try again")
      } else {
        Util.pr(this, "Credentials saved")
        Credentials.store(this, c)

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
}
