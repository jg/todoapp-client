package com.android.todoapp

import android.content.Context
import android.widget.{TextView}
import android.widget.{FilterQueryProvider}
import android.widget.CursorAdapter
import android.database.Cursor
import android.view.{ViewGroup, View, LayoutInflater}
import android.app.Activity
import com.android.todoapp.Implicits._
import android.graphics.Color

class TaskAdapter(context: Context, cursor: Cursor) extends CursorAdapter(context, cursor) {

  def showIncompleteTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is null order by priority desc", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def showCompletedTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is not null order by priority desc", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def getTask(i: Integer): Task = {
    val cursor: Cursor = getItem(i).asInstanceOf[Cursor]
    val task = new Task(cursor.getString(1))

    task.id = cursor.getInt(0)
    task.body = cursor.getString(2)
    task.completed_at = Some(cursor.getLong(3).asInstanceOf[Long])
    task.created_at = cursor.getLong(4).asInstanceOf[Long]
    task.updated_at = cursor.getLong(5).asInstanceOf[Long]

    task
  }

  override def bindView(view: View, context: Context, cursor: Cursor) {
    // set appropriate task priority color
    def setTaskPriority(id: Int): Unit = {
      val v = view.findViewById(id).asInstanceOf[View]
      cursor.getInt(7) match {
        case 1 => v.setBackgroundColor(Color.RED)
        case -1 => v.setBackgroundColor(Color.BLUE)
        case _ => v.setBackgroundColor(Color.BLACK)
      }
    }

    view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(cursor.getString(1))
    setTaskPriority(R.id.taskPriority)
  }

  override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
    val inflater = LayoutInflater.from(context)
    val v = inflater.inflate(R.layout.task, parent, false)
    bindView(v, context, cursor)

    v
  }

}
