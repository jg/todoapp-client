package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.content.Intent
import Implicits._

class TasksNew extends Activity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.tasks_new)

  }

  def saveTaskButtonHandler(view: View) {
    val task = new Task(findViewById(R.id.title).asInstanceOf[TextView])
    Tasks.add(this, task)
    startActivity(new Intent(this, classOf[MainActivity]))
  }
}
