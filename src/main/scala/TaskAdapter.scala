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
    Task.fromCursor(cursor)
  }

  override def bindView(view: View, context: Context, cursor: Cursor) {
    val task = Task.fromCursor(cursor)

    def setTaskPriority(): Unit = {
      val v = view.findViewById(R.id.taskPriority)

      if (!task.priority.isEmpty) {
        task.priority.get.toString match {
          case "high" => v.setBackgroundColor(Color.RED)
          case "low"  => v.setBackgroundColor(Color.BLUE)
          case _      => v.setBackgroundColor(Color.BLACK)
        }
      } else {
        v.setBackgroundColor(Color.BLACK)
      }
    }

    def setTaskDueDate() = {
      if (!task.due_date.isEmpty) {
        val v = view.findViewById(R.id.dueDate).asInstanceOf[TextView]
        val date = task.due_date.get
        v.setText(
          if (date.isToday)
            "Today"
          else
            date.dayMonthFormat
        )
      }
    }

    def setTaskList() = {
      val v = view.findViewById(R.id.taskList).asInstanceOf[TextView]
      v.setText(task.task_list)
    }

    view.findViewById(android.R.id.text1).asInstanceOf[TextView].setText(task.title)
    setTaskPriority()
    setTaskDueDate()
    setTaskList()
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
