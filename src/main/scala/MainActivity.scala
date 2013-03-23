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

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  var context: Context   = _
  var taskList: TaskListView = _

  var newTaskForm: NewTaskForm = _
  var commandButton: CommandButton = _
  val taskTable = TaskTable(this)

  override def onCreate(bundle: Bundle) {
    taskTable.open()

    super.onCreate(bundle)
    setContentView(R.layout.main)

    // Init widgets
    context = this
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

    val timer = new Timer()
    val timerTask = new RestorePostponedTask(this, adapter)
    timer.schedule(timerTask, 1000, 1000)
  }

  class RestorePostponedTask(context: Context, taskAdapter: TaskAdapter) extends TimerTask {
    def run() = {
      context.asInstanceOf[Activity].runOnUiThread(new Runnable() {
        override def run() {
          val tasks = taskAdapter.allTasks
          // restore postponed tasks that are ready
          val readyTasks = tasks.filter((t: Task) => t.isPostponeOver)
          val readyCount = readyTasks.size
          readyTasks.foreach((t: Task) => {
            t.resetPostpone()
            t.save(context)
          })

          if (readyCount > 0)
            Util.pr(context, "Restored " + readyCount.toString + " tasks from postponed state")
        }
      })
    }
  }

  override def onDestroy() = taskTable.close()

  override def onBackPressed() = newTaskForm.hide()

  override def onPause() = {
    super.onPause()
    taskTable.close()
  }

  override def onResume() = {
    super.onPause()
    taskTable.open()

    val tasks = Tasks.adapter(this).allTasks

    tasks.foreach((t: Task) => {
      Log.i(t.updated_at.fullFormat)
    })

    // restore repeating tasks that are ready
    tasks
      .filter((t: Task) => t.isReadyToRepeat)
      .foreach((t: Task) => {
        t.repeatTask()
        t.save(this)
      })
  }

  def initSyncButton(listView: ListView, id: Int) = findButton(id).setOnClickListener(onClickListener(synchronizeButtonHandler))

  def synchronizeButtonHandler(view: View): Unit = {
    def findTask(task: Task): Option[Task] = Tasks.adapter(this).allTasks.find(_.created_at == task.created_at)

    def getTasks(tasks: Collection) = {
      Log.i("-------------------------- get from server -----------------------------------")
      // get tasks from server
      for (item <- tasks.items.flatten) {
        val taskParams =
          for (data <- item.data.flatten; value <- data.value; name = data.name)
            yield (name, value): (String, Any)

        val task = Task.deserialize(taskParams)


        val dbTask = findTask(task)
        if (dbTask.isEmpty) { // task from server not present in db
          Log.i(task.title + " with ctime " + task.created_at.completeFormat + " not present in the db, inserting")
          taskTable.insert(task)
        } else {
          if (task.updated_at.getMillis > dbTask.get.updated_at.getMillis) { // newer task from server
            Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is newer, updating")
            taskTable.update(task)
          } else {
            Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is older")
          }
        }

      }
    }

    def sendTasks(tasks: Collection, username: String, password: String) = {
      Log.i("-------------------------- send to server -----------------------------------")
      // send tasks to server
      val expectedParams = tasks.template.get.map(_.name)
      val adapter = Tasks.adapter(this)

      adapter.allTasks.foreach((task: Task) => {
        Log.i("sent " + task.toJSON(List()) + " to server")
        val json = task.toJSON(expectedParams)
        Collection.postJSON(tasks.href, username, password, json)
      })
    }

    val username = "juliusz.gonera@gmail.com"
    val password = "testtest"
    try {
      val collection = Collection("http://polar-scrubland-5755.herokuapp.com/", username, password)
      // val collection = Collection("http://192.168.0.13:3000", username, password)

      for (links <- collection.links; taskLink <- links if taskLink.rel == "tasks" ) {
        val collection = Collection(taskLink.href, username, password)

        getTasks(collection)
        sendTasks(collection, username, password)
      }
    } catch {
      case e: java.net.UnknownHostException => Util.pr(this, "No connection")
    }
  }
}
