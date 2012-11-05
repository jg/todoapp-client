package com.android.todoapp

import android.app.Activity
import android.os.Bundle

import android.view.View;
import android.widget.Toast;

class TasksNew extends Activity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.tasks_new)

  }
}
