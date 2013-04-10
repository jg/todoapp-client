package com.android.todoapp
import android.content.Context
import android.app.ProgressDialog
import android.content.SharedPreferences

case class Credentials(username: String, password: String)

object Credentials {

  def isCorrect(c: Credentials, context: Context): Boolean = {
    HttpAgent.get(App.host, c.username, c.password)
    HttpAgent.statusCode == 200
  }

  def store(context: Context, c: Credentials) = {
    val storage = PreferenceStorage(context)
    storage.put("username", c.username)
    storage.put("password", c.password)
  }

  def get(context: Context): Option[Credentials] = {
    val storage = PreferenceStorage(context)

    val username = storage.get("username")
    val password = storage.get("password")
    if (username.isDefined && password.isDefined)
     Some(Credentials(username.get, password.get))
    else None
  }

}
