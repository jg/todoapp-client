package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor
import android.app.ProgressDialog
import android.os.AsyncTask

object Tasks {
  def adapter(context: Context): TaskAdapter = TaskAdapter(context, TaskTable(context).cursor)

  def add(c: Context, task: Task) = {
    task.id = TaskTable(c).insert(task)
    refresh(c)
  }

  def update(c: Context, task: Task) = {
    TaskTable(c).update(task)
    refresh(c)
  }

  def refresh(c: Context) {
    adapter(c).notifyDataSetChanged()
    adapter(c).getFilter().filter("")
  }

  def findTask(task: Task)(implicit context: Context): Option[Task] = Tasks.adapter(context).allTasks.find(_.created_at == task.created_at)

  def getTasks(tasks: Collection)(implicit context: Context, taskTable: TaskTable) = {
    Log.i("-------------------------- get from server -----------------------------------")
    // get tasks from server
    for (item <- tasks.items.flatten) {
      val taskParams =
        for (data <- item.data.flatten; value <- data.value; name = data.name)
          yield (name, value): (String, Any)

      val task = Task.deserialize(taskParams)


      val dbTask = findTask(task)
      if (dbTask.isEmpty) { // task from server not present in db
        Log.i(task.title + " with ctime " + task.created_at.completeFormat + " not present in the db, inserting")
        taskTable.insert(task)
      } else {
        if (task.updated_at.getMillis > dbTask.get.updated_at.getMillis) { // newer task from server
          Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is newer, updating")
          taskTable.update(task)
        } else {
          Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is older")
        }
      }

    }
  }

  class CheckCredentialsTask(context: Context, taskTable: TaskTable) extends MyAsyncTask[Credentials, Void, Boolean] {
    var progressDialog: ProgressDialog = _
    var credentials: Credentials = _

    override def onPreExecute() =
      progressDialog = ProgressDialog.show(context, "", "Checking credentials...", true)

    override def onPostExecute(isCorrect: Boolean) = {
      progressDialog.dismiss()
      if (isCorrect) {
        Util.pr(context, "Credentials saved")
        Credentials.store(context, credentials)
      } else {
        Util.pr(context, "Credentials not correct, try again")
      }
    }

    override def doInBackground(c: Credentials): Boolean =  {
      credentials = c
      Credentials.isCorrect(c, context)
    }
  }

  class SynchronizeTask(context: Context, taskTable: TaskTable) extends MyAsyncTask[Credentials, Void, Void] {
    var progressDialog: ProgressDialog = _

    override def doInBackground(c: Credentials): Void = {
      try {
        implicit val con: Context = context
        implicit val t: TaskTable = taskTable
        val username = c.username
        val password = c.password
        val collection = Collection("http://polar-scrubland-5755.herokuapp.com/", username, password)

        for (links <- collection.links; taskLink <- links if taskLink.rel == "tasks" ) {
          val collection = Collection(taskLink.href, username, password)

          getTasks(collection)
          sendTasks(collection, username, password)
        }

      } catch {
        case e: java.net.UnknownHostException => Util.pr(context, "No connection")
      }
      null
    }

    override def onPreExecute() =
      progressDialog = ProgressDialog.show(context, "", "Synchronization in progress...", true);

    override def onPostExecute(v: Void) =
      progressDialog.dismiss()
  }

  def sendTasks(tasks: Collection, username: String, password: String)(implicit context: Context) = {
    Log.i("-------------------------- send to server -----------------------------------")
    // send tasks to server
    val expectedParams = tasks.template.get.map(_.name)
    val adapter = Tasks.adapter(context)

    adapter.allTasks.foreach((task: Task) => {
      Log.i("sent " + task.toJSON(List()) + " to server")
      val json = task.toJSON(expectedParams)
      Collection.postJSON(tasks.href, username, password, json)
    })
  }


  def checkCredentials(c: Credentials)(implicit context: Context, taskTable: TaskTable): Boolean = {
    (new CheckCredentialsTask(context, taskTable)).execute(c).get()
  }

  def synchronize(c: Credentials)(implicit context: Context, taskTable: TaskTable) = {
    (new SynchronizeTask(context, taskTable)).execute(c)
  }


}
