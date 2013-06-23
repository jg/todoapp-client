package com.android.todoapp
import android.content.Context
import android.os.IBinder
import android.view.inputmethod.InputMethodManager
import android.widget.Toast

import android.app.Application

import scala.collection.mutable.Queue

class App extends Application {
}

object App {
  def host = "http://polar-scrubland-5755.herokuapp.com/"
  val DbVersion = 104
}
