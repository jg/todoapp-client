package com.android.todoapp

import android.content.Context
import android.app.ProgressDialog
import android.content.SharedPreferences

object PreferenceStorage {
  def apply(context: Context) = new PreferenceStorage(context)
}

class PreferenceStorage(context: Context) {
  val PrefsName = "todoapp"
  val settings = context.getSharedPreferences(PrefsName, 0);

  def put(key: String, value: String) = {
    val editor = settings.edit();
    editor.putString(key, value)
    editor.commit()
  }

  def get(key: String): Option[String] = {
    val value = settings.getString(key, "") 
    if (value != "")
      Some(value)
    else None
  }
  
  def contains(key: String) = settings.contains(key)
}
