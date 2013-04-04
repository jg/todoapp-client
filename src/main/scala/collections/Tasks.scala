package com.android.todoapp

import scala.collection.mutable.ListBuffer
import android.content.Context
import android.widget.{ArrayAdapter, CursorAdapter, SimpleCursorAdapter}
import android.database.Cursor
import android.app.ProgressDialog
import android.os.AsyncTask
import android.database.sqlite.SQLiteDatabase

object Tasks {
  def adapter(implicit c: Context): TaskAdapter = {
    implicit val conn: SQLiteDatabase = DBHelper.getDB(c)
    TaskAdapter(c, TaskTable().cursor)
  }

  def all(implicit context: Context): Seq[Task] = table.all

  private def table(implicit c: Context): TaskTable = TaskTable()

  def inList(list: String)(implicit c: Context): Seq[Task] = all.filter((task: Task) => task.task_list == list)

  def findTask(task: Task)(implicit context: Context): Option[Task] = Tasks.adapter.allTasks.find(_.created_at == task.created_at)

  def getTasks(tasks: Collection)(implicit context: Context) = {
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
        TaskTable().insert(task)
      } else {
        if (task.updated_at.getMillis > dbTask.get.updated_at.getMillis) { // newer task from server
          Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is newer, updating")
          TaskTable().update(task)
        } else {
          Log.i(task.title + " with ctime " + task.created_at.completeFormat + " found in db, server one is older")
        }
      }

    }
  }

  class CheckCredentialsTask(context: Context) extends MyAsyncTask[Credentials, Void, Boolean] {
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

  class SynchronizeTask(context: Context) extends MyAsyncTask[Credentials, Void, Void] {
    var progressDialog: ProgressDialog = _

    override def doInBackground(c: Credentials): Void = {
      try {
        implicit val con: Context = context
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

    Tasks.adapter.allTasks.foreach((task: Task) => {
      Log.i("sent " + task.toJSON(List()) + " to server")
      val json = task.toJSON(expectedParams)
      Collection.postJSON(tasks.href, username, password, json)
    })
  }


  def checkCredentials(c: Credentials)(implicit context: Context): Boolean = {
    (new CheckCredentialsTask(context)).execute(c).get()
  }

  def synchronize(c: Credentials)(implicit context: Context) = {
    (new SynchronizeTask(context)).execute(c)
  }

  def restorePostponed()(implicit context: Context) = { // restore postponed tasks that are ready
    val tasks = adapter.allTasks
    val readyTasks = tasks.filter((t: Task) => t.isPostponeOver)
    val readyCount = readyTasks.size
    readyTasks.foreach((t: Task) => {
      t.resetPostpone()
      t.save()
    })

    if (readyCount > 0)
      Util.pr(context, "Restored " + readyCount.toString + " tasks from postponed state")
  }

  def restoreRepeating()(implicit context: Context) = { // restore repeating tasks that are ready
    val tasks = adapter.allTasks
    val readyTasks = tasks.filter((t: Task) => t.isReadyToRepeat)
    val readyCount = readyTasks.size
    readyTasks.foreach((t: Task) => {
      t.repeatTask()
      t.save()
    })

    if (readyCount > 0)
      Util.pr(context, "Restored " + readyCount.toString + " recurring tasks from completed state")
  }



}
