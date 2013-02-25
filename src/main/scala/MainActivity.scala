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
  var commandButton: CommandButton = _

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    setTitle()
    context = this
    initListView()
    initSyncButton(listView, R.id.synchronizeButton)
    adapter.showIncompleteTasks(context)

    val container = findViewById(R.id.container)

    new Tabs(this, container)
    newTaskForm = new NewTaskForm(this, container, getResources(), getSupportFragmentManager())
    commandButton = new CommandButton(context, container, listView, R.id.commandButton)

  }
  override def onDestroy() = TaskTable(this).close()

  // Initializers
  def setTitle() = {
    val textView = findViewById(R.id.title).asInstanceOf[TextView]
    textView.setText("master")
  }

  def initSyncButton(listView: ListView, id: Int) = findButton(id).setOnClickListener(onClickListener(synchronizeButtonHandler))

  def initListView() = {
    listView = findListView(R.id.taskList)

    val adapter = Tasks.adapter(context)

    adapter.registerCheckBoxStateChangeHandler((buttonView: CompoundButton, isChecked: Boolean) =>
      commandButton.init(R.id.commandButton))

    adapter.registerTaskClickHandler((taskCursorPosition: Int) =>  {
      val intent = new Intent(this, classOf[TaskEditActivity])
      intent.putExtra("taskPosition", taskCursorPosition);
      startActivity(intent)
    })

    listView.setAdapter(adapter)
  }

  // Button handlers


  override def onBackPressed() = newTaskForm.hide()

  def synchronizeButtonHandler(view: View) = {
    Log.i("clicked synchronize!")
    val collection = Collection("http://polar-scrubland-5755.herokuapp.com", "juliusz.gonera@gmail.com", "testtest")
    collection.links.map(links => links.find(_.rel == "tasks").map(l => Util.pr(this, l.href)))
  }
  def adapter = listView.getAdapter()

}
