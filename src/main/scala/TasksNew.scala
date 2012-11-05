package com.android.todoapp

import _root_.android.app.Activity
import _root_.android.os.Bundle

import _root_.android.view.View;
import _root_.android.widget.Toast;

class TasksNew extends Activity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.tasks_new)

  }
}
