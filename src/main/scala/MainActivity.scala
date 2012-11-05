package com.android.todoapp

import android.app.Activity
import android.os.Bundle

import android.view.View;
import android.widget.Toast;
import android.content.Intent;

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
