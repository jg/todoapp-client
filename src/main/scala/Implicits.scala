package com.android.todoapp

import android.widget.{TextView, Button, ListView, ListAdapter}
import android.view.View

object Implicits {
  implicit def textViewToString(tv: TextView): String = tv.getText.toString
  implicit def listAdapterToTaskAdapter(v: ListAdapter): TaskAdapter = v.asInstanceOf[TaskAdapter]
}
