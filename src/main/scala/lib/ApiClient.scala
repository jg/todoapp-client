package com.android.todoapp

import java.lang.IllegalArgumentException
import java.util.ArrayList
import android.content.Context

class ApiClient(rootUrl: String, userName: String, password: String) {
  lazy val collection = Collection(get(rootUrl))
  // ApiClient State enum
  object State extends Enumeration {
    type State = Value
    val START, GET_TASKS = Value
  }
  import State._

  var mState: State = START

  def get(url: String): String = HttpAgent.get(url, userName, password)

  def postJSON(url: String, json: String): String = HttpAgent.postJSON(url, userName, password, json)

  def taskUrl: Option[String] = {
    collection.links.flatMap(links =>
      links.find(_.rel == "tasks").map(link => link.href))
  }

  def taskCollection: Option[Collection] = taskUrl.map(url => Collection(get(url)))

  def getTasks()(implicit c: Context): List[Task] = {
    for (taskItem <- taskCollection.get.items.get)
      yield Task.deserialize(
        for (dataItem <- taskItem.data.get;
             value <- dataItem.value) yield (dataItem.name, value)
        )
  }

  def putTasks = {
    val url = taskUrl
  }

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
