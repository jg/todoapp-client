package com.android.todoapp

import android.widget.{Button, ListView, TextView, EditText}
import android.app.Activity
import android.view.{View, KeyEvent}
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.os.IBinder

trait Finders extends Activity {
  def findSpinner(id: Int): Spinner = findViewById(id).asInstanceOf[Spinner]
  def findButton(id: Int): Button = findViewById(id).asInstanceOf[Button]
  def findListView(id: Int) = findViewById(id).asInstanceOf[ListView]
  def findEditText(id: Int) = findViewById(id).asInstanceOf[EditText]
  def findTextView(id: Int) = findViewById(id).asInstanceOf[TextView]
}

trait ActivityExtensions extends Activity with Finders {
  def showKeyboard() = {
    val imm =  getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
  }

  def hideKeyboard(windowToken: IBinder) = {
    val imm =  getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.hideSoftInputFromWindow(windowToken, 0);
  }

}

object Utils {
  def onClickListener(f: (View) => Unit) =
    new View.OnClickListener() { override def onClick(v: View) = f(v) }

  def onEditorActionListener(f: (TextView, Int, KeyEvent) => Boolean) =
    new TextView.OnEditorActionListener() {
      override def onEditorAction(v: TextView, actionId: Int, event: KeyEvent): Boolean =
        f(v, actionId, event)
    }

}

object Log {
  def i(msg: String) { android.util.Log.i("todoapp", msg) }
}


