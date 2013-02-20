package com.android.todoapp

import android.widget.{TextView, Button, ListView, ListAdapter}
import android.view.{View, KeyEvent}
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.CompoundButton

object Implicits {
  implicit def date2long(d: Date): Long = d.getMillis
  implicit def textViewToString(tv: TextView): String = tv.getText.toString
  implicit def listAdapterToTaskAdapter(v: ListAdapter): TaskAdapter = v.asInstanceOf[TaskAdapter]

  implicit def fToOnEditorActionListener(f: (TextView, Int, KeyEvent) => Boolean) = new TextView.OnEditorActionListener() {
    override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean = {
      f(v, actionId, event)
    }
  }

  implicit def LambdaToOnClickListener(f: (View) => Unit) = new View.OnClickListener() {
    def onClick(v: View) = f(v)
  }

  implicit def LambdaToOnCheckedChangeListener(f: (CompoundButton, Boolean) => Unit) = new OnCheckedChangeListener() {
    def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) = f(buttonView, isChecked)
  }

}
