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

class MainActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  var context: Context   = _
  var listView: ListView = _

  var newTaskForm: NewTaskForm = _

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    setTitle()
    context = this
    initListView()
    initCommandButton(listView, R.id.commandButton)
    initSyncButton(listView, R.id.synchronizeButton)
    adapter.showIncompleteTasks(context)

    val container = findViewById(R.id.container)
    new Tabs(this, container, adapter)
    newTaskForm = new NewTaskForm(this, container, getResources(), getSupportFragmentManager())

  }
  override def onDestroy() = TaskTable(this).close()

  // Initializers
  def setTitle() = {
    val textView = findViewById(R.id.title).asInstanceOf[TextView]
    textView.setText("master")
  }
  def initSyncButton(listView: ListView, id: Int) = findButton(id).setOnClickListener(onClickListener(synchronizeButtonHandler))
  def initCommandButton(listView: ListView, id: Int) = {
    def initAddNewTaskButton(id: Int) = {
      val b = findButton(id)

      b.setText("+")
      b.setOnClickListener(addNewTaskButtonHandler(_: View))
    }
    def initMarkTasksAsCompleteButton(id: Int) = {
      val b = findButton(id)

      b.setText("✓")
      b.setOnClickListener(markTaskAsCompleteHandler(_: View))
    }
    def checkedItemCount(lv: ListView): Integer = {
      Range(0, lv.getChildCount()).count(lv.getChildAt(_).asInstanceOf[TaskLayout].isChecked())
    }

    if (checkedItemCount(listView) > 0) {
      initMarkTasksAsCompleteButton(id)
    } else {
      initAddNewTaskButton(id)
    }
  }
  def initListView() = {
    listView = findListView(R.id.taskList)

    val adapter = Tasks.adapter(context)

    adapter.registerCheckBoxStateChangeHandler((buttonView: CompoundButton, isChecked: Boolean) =>
      initCommandButton(listView, R.id.commandButton))

    adapter.registerTaskClickHandler((taskCursorPosition: Int) =>  {
      val intent = new Intent(this, classOf[TaskEditActivity])
      intent.putExtra("taskPosition", taskCursorPosition);
      startActivity(intent)
    })

    listView.setAdapter(adapter)
  }

  // Button handlers

  def views(lv: ListView) = Range(0, lv.getChildCount()).map(lv.getChildAt(_))

  def items(lv: ListView) = Range(0, lv.getChildCount()).map(lv.getChildAt(_))

  override def onBackPressed() = newTaskForm.hide()

  def markTaskAsCompleteHandler(view: View) {
    def checkedItems(lv: ListView) = {
      Range(0, lv.getChildCount())
        .filter(lv.getChildAt(_).asInstanceOf[TaskLayout].isChecked())
        .map(lv.getAdapter().getTask(_))
    }
    def markTask(context: Context, task: Task) {
      task.markAsCompleted()
      task.save(context)
    }
    def unCheckAllItems(v: View) = views(listView).map(_.asInstanceOf[TaskLayout].setChecked(false))

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

  def addNewTaskButtonHandler(view: View) = {
    def showNewTaskForm() = {
      findViewById(R.id.tasksNew).setVisibility(View.VISIBLE)
      val input = findEditText(R.id.task_title_input)
      input.setText("")
      input.requestFocus()
      showKeyboard()
    }
    showNewTaskForm()
  }

  def adapter = listView.getAdapter()

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

}
