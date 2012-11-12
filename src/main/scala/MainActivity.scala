package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.{Toast, ListView, Button, AdapterView}
import android.widget.AdapterView.{OnItemClickListener, OnItemSelectedListener}
import android.content.{Intent, Context}
import collection.JavaConversions._
import android.util.SparseBooleanArray

import com.android.todoapp.Utils._

class MainActivity extends Activity with TypedActivity {
  object Intents  {
    val TASKS_NEW   = "com.android.todoapp.TasksNew"
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    val listView =  findViewById(R.id.taskList).asInstanceOf[ListView]
    listView.setAdapter(Tasks.adapter(this))
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

    listView.setOnItemClickListener(new OnItemClickListener() {
      override def onItemClick(adapter: AdapterView[_], view: View, position: Int, arg: Long) = {
        setMarkAsCompleteButtonVisibility(listView, R.id.markTaskAsComplete)
      }
    })
  }

  override def onBackPressed() {}

  def addNewTaskButtonHandler(view: View) {
    startActivity(new Intent(this, classOf[TasksNew]))
  }

  def setMarkAsCompleteButtonVisibility(listView: ListView, id: Integer) = {
    val b: Button = findViewById(id).asInstanceOf[Button]
    val count     = checkedItemCount(listView)

    if (count > 0) {
      b.setVisibility(View.VISIBLE)
    } else {
      b.setVisibility(View.INVISIBLE)
    }
  }

  def checkedItems(listView: ListView): Array[Task] = {
    val adapter: TaskAdapter = listView.getAdapter().asInstanceOf[TaskAdapter]
    val checkedItems: SparseBooleanArray = listView.getCheckedItemPositions()

    val checkedIndices = Range(0, checkedItems.size()).filter(i => checkedItems.valueAt(i))

    checkedIndices.map(i => adapter.getTask(checkedItems.keyAt(i))).toArray
  }

  def checkedItemCount(listView: ListView): Integer = {
    val checkedItems: SparseBooleanArray = listView.getCheckedItemPositions()
    Range(0, checkedItems.size()).count(i => checkedItems.valueAt(i))
  }

  def pr(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
  }

}
