package com.android.todoapp

import _root_.android.app.Activity
import _root_.android.os.Bundle

import _root_.android.view.View;
import _root_.android.widget.Toast;
import _root_.android.content.Intent;

class MainActivity extends Activity with TypedActivity {

  object Intents  {
    val TASKS_NEW   = "com.android.todoapp.TasksNew"
  }

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.main)

  }

  def addNewTaskButtonHandler(view: View) {
    startActivity(new Intent(this, classOf[TasksNew]))
  }

  def pr(s: String) {
    Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
  }
}
