package com.android.todoapp
import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import android.app.Application

object Util {
  def showKeyboard(context: Context) = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
  }

  def hideKeyboard(context: Context, windowToken: IBinder) = {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE).asInstanceOf[InputMethodManager]
    imm.hideSoftInputFromWindow(windowToken, 0);
  }

  def pr(context: Context, s: String) = Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

}
