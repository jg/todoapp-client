package com.android.todoapp

import android.widget.{TextView, Button, ListView, ListAdapter}
import android.view.View

object Implicits {
  implicit def textViewToString(tv: TextView): String = tv.getText.toString
  implicit def viewToButton(v: View): Button = v.asInstanceOf[Button]
  implicit def viewToListView(v: View): ListView = v.asInstanceOf[ListView]
  implicit def listAdapterToTaskAdapter(v: ListAdapter): TaskAdapter = v.asInstanceOf[TaskAdapter]
}
