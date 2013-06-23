package com.android.todoapp

import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.{Intent, Context, SharedPreferences}
import android.os.{Bundle, Handler, IBinder}
import android.support.v4.app.{DialogFragment, FragmentActivity, FragmentManager}
import android.view.{Menu, MenuItem, View, LayoutInflater, KeyEvent}
import android.widget.{AbsListView, ArrayAdapter, Toast, ListView, Button, AdapterView, TextView, CheckedTextView, TabHost, CompoundButton}
import com.android.todoapp.Implicits._
import com.android.todoapp.Utils._

class TaskListActivity extends FragmentActivity with TypedActivity with ActivityExtensions {
  implicit val context: Context    = this

  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
  }
}
