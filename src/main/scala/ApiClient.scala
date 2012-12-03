package com.android.todoapp

import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException
import java.lang.IllegalArgumentException
import java.util.ArrayList

object ApiClient {
  val rootUrl = "http://polar-scrubland-5755.herokuapp.com/"

  // ApiClient State enum
  object State extends Enumeration {
    type State = Value
    val START, GET_TASKS = Value
  }
  import State._

  var mState: State = START
  var mLogin = ""
  var mPassword = ""

  def setLogin(login: String) = mLogin = login

  def setPassword(password: String) = mPassword = password

  def get(url: String, username: String, password: String): String = HttpAgent.get(url, username, password)

  def authenticatedGet(url: String): String = {
    if ((mLogin != null) && (mPassword != null))
      get(url, mLogin, mPassword)
    else
      throw new IllegalArgumentException("Login data not set")
  }

  def findResourceUrl(resourceName: String): String = ""

  def findTaskListUrl(): String = path("/tasks")

  def path(path: String): String = rootUrl + path

  /*
  def getTaskList(): ArrayList[Task] = {
    @throws(classOf[IOException])
    var tasks = new ArrayList[Task](10)
    val json = authenticatedGet(findTaskListUrl())

    val obj  = new JSONObject(json)
    val items = obj.getJSONObject("collection").getJSONArray("items")
    for (i <- 0 to items.length()) {
      val item = items.getJSONObject(i)
      tasks.add(Task.fromJSON(item.toString()))
    }

    tasks
  }
  */

  def isUserAuthenticated(login: String, password: String) = {
    HttpAgent.get(rootUrl, login, password)

    HttpAgent.statusCode == 200
  }

}
