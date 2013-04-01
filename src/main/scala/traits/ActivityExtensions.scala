package com.android.todoapp

import android.widget.{Button, ListView, TextView, EditText}
import android.app.Activity
import android.view.{View, KeyEvent}
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.os.IBinder
import android.database.sqlite.SQLiteDatabase

trait ActivityExtensions extends Activity with Finders {
  implicit var conn: SQLiteDatabase = null
  implicit val context: Context   = this

  def showKeyboard() = {
    val imm =  getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
  }

  def hideKeyboard(windowToken: IBinder) = {
    val imm =  getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.hideSoftInputFromWindow(windowToken, 0);
  }

  def app = getApplicationContext.asInstanceOf[App]

  override def onStart() = {
    super.onStart()
    conn = DBHelper.getDB(this)
  }
}

