package com.android.todoapp

import android.content.Context
import android.widget.{TextView, CheckBox, FilterQueryProvider, CursorAdapter, CompoundButton}
import android.database.Cursor
import android.view.{ViewGroup, View, LayoutInflater}
import android.app.Activity
import com.android.todoapp.Implicits._
import android.graphics.Color
import com.android.todoapp.Utils._
import android.widget.Toast
import android.graphics.Paint

class TaskAdapter(context: Context, cursor: Cursor) extends CursorAdapter(context, cursor) {
  var checkBoxStateChangeHandler: Option[(CompoundButton, Boolean) => Unit] = None
  var taskClickHandler: Option[(Int) => Unit] = None
  var currentQuery: Option[String] = None

  def showIncompleteTasks() = {
    for (query <- currentQuery) {
      currentQuery = Some(query.replace("completed_at is not null", "completed_at is null"))
      filter(currentQuery.get)
    }
  }

  def showCompletedTasks() = {
    for (query <- currentQuery) {
      currentQuery = Some(query.replace("completed_at is null", "completed_at is not null"))
      filter(currentQuery.get)
    }
  }

  def showTasksDueToday(context: Context) = {
    currentQuery = Some("select * from tasks where completed_at is null and due_date = date('now')" + ordering)
    filter(currentQuery.get)
  }

  def showTasksDueThisWeek() = {
    currentQuery = Some("select * from tasks where completed_at is null and strftime('%W', due_date) = strftime('%W', 'now')" + ordering)
    filter(currentQuery.get)
  }

  def showTasksInList(list: String) = {
    currentQuery = Some("select * from tasks where completed_at is null and task_list = '" + list + "'" + ordering)
    filter(currentQuery.get)
  }

  def filterWihCurrentQuery() = for (query <- currentQuery) filter(query)

  def getTask(i: Integer): Task = {
    // TODO: refactor Task#fromCursor?
    val cursor: Cursor = getItem(i).asInstanceOf[Cursor]
    Task.fromCursor(cursor)
  }

  def registerCheckBoxStateChangeHandler(f: (CompoundButton, Boolean) => Unit) = checkBoxStateChangeHandler = Some(f)
  def registerTaskClickHandler(f: (Int) => Unit) = taskClickHandler = Some(f)

  override def bindView(view: View, context: Context, cursor: Cursor) {
    val task = Task.fromCursor(cursor)

    def setTaskPriority(): Unit = {
      val v = view.findViewById(R.id.taskPriority)

      if (!task.priority.isEmpty) {
        task.priority.toString match {
          case "high" => v.setBackgroundColor(Color.RED)
          case "low"  => v.setBackgroundColor(Color.BLUE)
          case _      => v.setBackgroundColor(Color.BLACK)
        }
      } else {
        v.setBackgroundColor(Color.BLACK)
      }
    }

    def dateView = view.findViewById(R.id.dueDate).asInstanceOf[TextView]

    def setDate(date: Option[Date]) = date match {
      case Some(date) => {
        dateView.setText(
          if (date.isToday)
            "Today"
          else
            date.dayMonthFormat
        )
      }
      case None => dateView.setText("")
    }

    def setTaskList() = {
      val v = view.findViewById(R.id.taskList).asInstanceOf[TextView]
      v.setText(task.task_list)
    }

    def setTaskClickListener() = {
      val v = view.findViewById(R.id.taskTitle)
      val position = cursor.getPosition()
      v.setOnClickListener((v: View) =>
        for (handler <- taskClickHandler) handler(position))
    }

    def setTaskCheckboxToggleListener() = {
      val v = view.findViewById(R.id.taskCheckbox).asInstanceOf[CheckBox]
      v.setOnCheckedChangeListener(
        (buttonView: CompoundButton, isChecked: Boolean) => {
          for (handler <- checkBoxStateChangeHandler) handler(buttonView, isChecked)
        }
        )
    }

    def setTaskTitle() = taskTitle.setText(task.title)

    def taskTitle = view.findViewById(R.id.taskTitle).asInstanceOf[TextView]

    setTaskTitle()
    setTaskPriority()
    setTaskList()
    setTaskClickListener()
    setTaskCheckboxToggleListener()
    if (task.isCompleted) {
      taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG)
      setDate(task.completed_at)
    } else {
      setDate(task.due_date)
      taskTitle.setPaintFlags(0)
    }
  }

  override def newView(context: Context, cursor: Cursor, parent: ViewGroup): View = {
    val inflater = LayoutInflater.from(context)
    val v = inflater.inflate(R.layout.task, parent, false)
    bindView(v, context, cursor)

    v
  }

  private def columnIndex(fieldName: String) = Task.columnIndex(fieldName)

  private def pr(s: String) = Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

  private def ordering = " order by due_date asc, priority desc"

  private def filter(query: String) = {
    setFilterQueryProvider((_:CharSequence) =>
      (new TaskTable(context)).db.rawQuery(query, null))
    getFilter().filter("")
    Tasks.refresh(context)
  }

}
