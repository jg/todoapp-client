package com.android.todoapp
import android.content.Context
import android.app.ProgressDialog
import android.content.SharedPreferences

case class Credentials(username: String, password: String)

object Credentials {
  val PrefsName = "todoapp"

  def isCorrect(c: Credentials, context: Context): Boolean = {
    HttpAgent.get(App.host, c.username, c.password)
    HttpAgent.statusCode == 200
  }

  def store(context: Context, c: Credentials) = {
    val settings = context.getSharedPreferences(PrefsName, 0);
    val editor = settings.edit();
    editor.putString("username", c.username);
    editor.putString("password", c.password);
    editor.commit()
  }

  def get(context: Context): Option[Credentials] = {
    val settings = context.getSharedPreferences(PrefsName, 0);
    val username = settings.getString("username", "")
    val password = settings.getString("password", "")
    if (username != "" && password != "")
     Some(Credentials(username, password))
    else None
  }

}
