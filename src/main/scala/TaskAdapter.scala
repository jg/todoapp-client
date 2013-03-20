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

object TaskAdapter {
  var adapter: Option[TaskAdapter] = None

  def apply(context: Context, cursor: Cursor): TaskAdapter = {
    if (adapter.isEmpty) adapter = Some(new TaskAdapter(context, cursor))
    adapter.get
  }
}

class TaskAdapter(context: Context, cursor: Cursor) extends CursorAdapter(context, cursor) {
  var checkBoxStateChangeHandler: Option[(CompoundButton, Boolean) => Unit] = None
  var taskClickHandler: Option[(Int) => Unit] = None
  var currentQuery: Option[String] = None
  val taskTable = TaskTable(context)

  val incompleteTasksQueryWhere =  "completed_at is null and postpone is null"
  val completedTasksQueryWhere =  "completed_at is not null or postpone is not null"

  def showIncompleteTasks() = {
    for (query <- currentQuery) {
      currentQuery = Some(query.replace(completedTasksQueryWhere, incompleteTasksQueryWhere))
      filterWithCurrentQuery()
    }
  }

  def showCompletedTasks() = {
    for (query <- currentQuery) {
      currentQuery = Some(query.replace(incompleteTasksQueryWhere, completedTasksQueryWhere))
      filterWithCurrentQuery()
    }
  }

  def defaultWhere = " where completed_at is null and postpone is null "

  def taskListWhere(list: String) = " and task_list = '" + list + "' "

  def showTasksDueToday() = {
    currentQuery = Some("select * from tasks " + defaultWhere + " and strftime('%Y-%m-%d', due_date) = date('now')" + ordering)
    filterWithCurrentQuery()
  }

  def showTasksDueThisWeek() = {
    currentQuery = Some("select * from tasks" + defaultWhere + "and strftime('%W', due_date) = strftime('%W', 'now')" + ordering)
    filterWithCurrentQuery()
  }

  def showTasksInList(list: String) = {
    currentQuery = Some("select * from tasks" + defaultWhere + taskListWhere(list) + ordering)
    filterWithCurrentQuery()
  }

  def showPostponedTasks(list: String) = {
    currentQuery = Some("select * from tasks where postpone is not null " + taskListWhere(list) + ordering)
    filterWithCurrentQuery()
  }

  def filterWithCurrentQuery() = {
    for (query <- currentQuery) {
      filter(query)
      Log.i(query)
    }
  }

  def getTask(i: Integer): Task = Task.fromCursor(getItem(i).asInstanceOf[Cursor])

  def allTasks: Seq[Task] = {
    // val cursor = taskTable.db.rawQuery("select * from tasks", null)
    val cursor = taskTable.db.query("tasks", null, null, null, null, null, null, null)
    val lst = scala.collection.mutable.ListBuffer.empty[Task]

    if (cursor.getCount() > 0) {
      cursor.moveToFirst()
      while (!cursor.isAfterLast()) {
        lst += Task.fromCursor(cursor)
        cursor.moveToNext()
      }

    }
    cursor.close()
    lst.toList
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
      taskTable.db.rawQuery(query, null))
    getFilter().filter("")
    Tasks.refresh(context)
  }

}
