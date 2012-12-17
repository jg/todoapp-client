package com.android.todoapp

import android.widget.{TextView, Button, ListView, ListAdapter}
import android.view.{View, KeyEvent}

object Implicits {
  implicit def date2long(d: Date): Long = d.getMillis
  implicit def textViewToString(tv: TextView): String = tv.getText.toString
  implicit def listAdapterToTaskAdapter(v: ListAdapter): TaskAdapter = v.asInstanceOf[TaskAdapter]

  implicit def fToOnEditorActionListener(f: (TextView, Int, KeyEvent) => Boolean) = new TextView.OnEditorActionListener() {
    override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean = {
      f(v, actionId, event)
    }
  }

}
