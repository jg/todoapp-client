package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.{View, LayoutInflater}
import android.widget.{Toast, ListView, Button, AdapterView, TextView}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray

import com.android.todoapp.Utils._
import com.android.todoapp.Implicits._

class MainActivity extends Activity with TypedActivity {
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
  }

  // Initializers

  def setContext() = context = this

  def initCommandButton(listView: ListView, id: Int) = {

    def initAddNewTaskButton(id: Int) = {
      val b: Button = findViewById(id).asInstanceOf[Button]

      b.setText("+")
      b.setOnClickListener(onClickListener(addNewTaskButtonHandler))
    }

    def initMarkTasksAsCompleteButton(id: Int) = {
      val b: Button = findViewById(id).asInstanceOf[Button]

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
    listView = findViewById(R.id.taskList).asInstanceOf[ListView]

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

    if (items.length == 1)
      pr("task marked as completed")
    else
      pr(items.length + " tasks marked as completed")
  }

  def addNewTaskButtonHandler(view: View) = startActivity(new Intent(this, classOf[TasksNew]))

  // Utility functions

  def onClickListener(f: (View) => Unit) =
    new View.OnClickListener() { override def onClick(v: View) = f(v) }

  def adapter = listView.getAdapter()

  def pr(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

  override def onBackPressed() {}
}
