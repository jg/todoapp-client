package com.android.todoapp

import android.app.Activity
import android.os.Bundle

import android.view.View
import android.widget.Toast
import android.widget.ListView
import android.content.Intent
import android.widget.ArrayAdapter
import collection.JavaConversions._

class MainActivity extends Activity with TypedActivity {

  object Intents  {
    val TASKS_NEW   = "com.android.todoapp.TasksNew"
  }

  var listView: ListView = _

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

    Tasks.loadFromDB(this)
    listView =  findViewById(R.id.task_list).asInstanceOf[ListView]
    var adapter: ArrayAdapter[Task] = new ArrayAdapter[Task](this, android.R.layout.simple_list_item_multiple_choice, Tasks())
    listView.setAdapter(adapter)

    listView.setItemsCanFocus(false);
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
  }

  def addNewTaskButtonHandler(view: View) {
    startActivity(new Intent(this, classOf[TasksNew]))
  }

  def pr(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
  }
}
