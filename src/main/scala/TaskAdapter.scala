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

  // TODO: this smells
  def showIncompleteTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is null order by due_date asc, priority desc", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def showCompletedTasks(context: Context) {
    setFilterQueryProvider(new FilterQueryProvider() {
      def runQuery(constraint: CharSequence): Cursor = {
        (new TaskTable(context)).db.rawQuery("select * from tasks where completed_at is not null order by due_date asc, priority desc", null)
      }
    })
    getFilter().filter("")
    Tasks.refresh(context)
  }

  def getTask(i: Integer): Task = {
    // TODO: refactor Task#fromCursor?
    val cursor: Cursor = getItem(i).asInstanceOf[Cursor]
    val task = new Task(cursor.getString(columnIndex("title")))

    task.id           = cursor.getInt(columnIndex("_id"))
    task.completed_at = Some(cursor.getString(columnIndex("completed_at")))
    task.created_at   = cursor.getString(columnIndex("created_at"))
    task.updated_at   = cursor.getString(columnIndex("updated_at"))
    task.due_date     = cursor.getString(columnIndex("due_date"))
    task.priority     = cursor.getInt(columnIndex("priority"))

    task
  }

  override def bindView(view: View, context: Context, cursor: Cursor) {
    // set appropriate task priority color
    def setTaskPriority(id: Int): Unit = {
      val v = view.findViewById(id)
      cursor.getInt(columnIndex("priority")) match {
        case 1 => v.setBackgroundColor(Color.RED)
        case -1 => v.setBackgroundColor(Color.BLUE)
        case _ => v.setBackgroundColor(Color.BLACK)
      }
    }

    def setTaskDueDate(id: Int) = {
      val v = view.findViewById(id).asInstanceOf[TextView]
      val date = Date.parse(cursor.getString(Task.columnIndex("due_date")))
      v.setText(
        if (date.isToday)
          "Today"
        else
          date.dayMonthFormat
      )
    }

    def setTaskList(id: Int) = {
      val v = view.findViewById(id).asInstanceOf[TextView]
      v.setText(cursor.getString(Task.columnIndex("task_list")))
    }

    val title = cursor.getString(columnIndex("title"))
    view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(title)
    setTaskPriority(R.id.taskPriority)
    setTaskDueDate(R.id.dueDate)
    setTaskList(R.id.taskList)
  }

  override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
    val inflater = LayoutInflater.from(context)
    val v = inflater.inflate(R.layout.task, parent, false)
    bindView(v, context, cursor)

    v
  }

  // Helpers

  def columnIndex(fieldName: String) = Task.columnIndex(fieldName)

}
