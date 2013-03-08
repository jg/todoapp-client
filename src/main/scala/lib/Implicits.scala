package com.android.todoapp

import android.view.{View, KeyEvent}
import android.widget.CompoundButton.OnCheckedChangeListener
import android.widget.CompoundButton
import android.view.View.OnFocusChangeListener
import android.widget._
import android.widget.AdapterView._
import android.database.Cursor

object Implicits {
  // implicit def date2long(d: Date): Long = d.getMillis
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

  implicit def LambdaToOnItemSelectedListener(f: (AdapterView[_], View, Int, Long) => Unit) = new AdapterView.OnItemSelectedListener() {
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long) = 
      f(parent, view, position, id)

    def onNothingSelected(parent: AdapterView[_]) {}
  }

  implicit def LambdaToOnCheckedChangeListener(f: (CompoundButton, Boolean) => Unit) = new OnCheckedChangeListener() {
    def onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) = f(buttonView, isChecked)
  }

  implicit def LambdaToOnFocusChangeListener(f: (View, Boolean) => Unit) = new View.OnFocusChangeListener() {
    def onFocusChange(v: View, hasFocus: Boolean) = f(v, hasFocus)
  }

  implicit def LambdaToFilterQueryProvider(f: (CharSequence) => Cursor) = new FilterQueryProvider() {
    def runQuery(constraint: CharSequence): Cursor = {
      f(constraint)
    }
  }
}
