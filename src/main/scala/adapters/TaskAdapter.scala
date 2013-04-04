package com.android.todoapp

import android.database.sqlite.SQLiteDatabase
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
import android.os.Handler

class DBQuery(queryGenerator: () => String) {
  def toSQL = queryGenerator()
}
object DBQuery { def apply(f: () => String) = new DBQuery(f)}

object TaskAdapter {
  private[this] var taskAdapter: Option[TaskAdapter] = None
  def apply(context: Context, cursor: Cursor): TaskAdapter = taskAdapter match {
    case Some(adapter) => adapter
    case None => {
      taskAdapter = Some(new TaskAdapter(context, cursor))
      taskAdapter.get
    }
  }
}

class TaskAdapter(context: Context, cursor: Cursor) extends CursorAdapter(context, cursor, true) with Refreshable {
  val taskTable = new TaskTable()
  var checkBoxStateChangeHandler: Option[(CompoundButton, Boolean) => Unit] = None
  var taskClickHandler: Option[(Int) => Unit] = None
  // var currentQuery: Option[String] = None
  var currentQuery: DBQuery = DBQuery(() => defaultQuery)
  var isShowingCompletedTasks = false
  implicit val c: Context = context

  def showIncompleteTasks() = {
    isShowingCompletedTasks = false
    filterWithCurrentQuery()
  }

  def showCompletedTasks() = {
    isShowingCompletedTasks = true
    filterWithCurrentQuery()
  }

  def dueToday = "strftime('%Y-%m-%d', due_date) = date('now')"

  def defaultQuery = selection + whereClause + " and " + dueToday + ordering

  def taskListWhere(list: String) = " and task_list = '" + list + "' "

  def completedTasksWhere = isShowingCompletedTasks match {
    case true => " completed_at is not null "
    case false => " completed_at is null "
  }

  def postponeWhereClause = isShowingCompletedTasks match {
    case true => " postpone is not null "
    case false => " postpone is null "
  }

  def whereClause =  isShowingCompletedTasks match {
    case true => "where (" + postponeWhereClause + "or" + completedTasksWhere + ") "
    case false => "where " + postponeWhereClause + "and" + completedTasksWhere + " "
  }

  def selection = "select tasks.*, task_lists.name as task_list from tasks inner join task_lists on task_lists._id = tasks.task_list_id "

  def showTasksDueToday() = {
    currentQuery = DBQuery(() =>
      selection + whereClause + " and " + dueToday + ordering
    )
    filterWithCurrentQuery()
  }

  def showTasksDueThisWeek() = {
    currentQuery = DBQuery(() =>
      selection + whereClause + "and strftime('%W', due_date) = strftime('%W', 'now')" + ordering)
    filterWithCurrentQuery()
  }

  def showTasksInList(list: String) = {
    currentQuery = DBQuery(() =>
      selection + whereClause + taskListWhere(list) + ordering)
    filterWithCurrentQuery()
  }

  def showPostponedTasks(list: String) = {
    currentQuery = DBQuery(() =>
      "select * from tasks where postpone is not null " + taskListWhere(list) + ordering)
    filterWithCurrentQuery()
  }

  def filterWithCurrentQuery() = {
    filter(currentQuery.toSQL)
    Log.i(currentQuery.toSQL)
  }

  def getTask(i: Integer): Task = Task.fromCursor(getItem(i).asInstanceOf[Cursor])

  def allTasks = taskTable.all

  def registerCheckBoxStateChangeHandler(f: (CompoundButton, Boolean) => Unit) = checkBoxStateChangeHandler = Some(f)
  def registerTaskClickHandler(f: (Int) => Unit) = taskClickHandler = Some(f)

  override def bindView(view: View, context: Context, cursor: Cursor) {
    val task = Task.fromCursor(cursor)
    val taskTitle = view.findViewById(R.id.taskTitle).asInstanceOf[TextView]

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

    def setTaskDueDate() = {
      if (task.isCompleted)
        setDate(task.completed_at)
      else {
        if (task.postpone.isDefined) {
          val hourDifference =
                task.updated_at.addPeriod(task.postpone.get).hourDifference(Date.now)
          if (hourDifference != 0) {
            dateView.setText("postponed for " + hourDifference.toString + " hours")
          } else {
            val minuteDifference =
              task.updated_at.addPeriod(task.postpone.get).minuteDifference(Date.now)
            dateView.setText("postponed for " + minuteDifference.toString + " minutes")
          }
        } else if (task.due_date.isDefined) {
          setDate(task.due_date)
        } else {
          dateView.setText("")
        }
      }
    }

    def setTaskPaintFlags() = {
      if (task.isCompleted)
        taskTitle.setPaintFlags(taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG)
      else
        taskTitle.setPaintFlags(taskTitle.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG))
    }

    setTaskTitle()
    setTaskPriority()
    setTaskList()
    setTaskClickListener()
    setTaskCheckboxToggleListener()
    setTaskDueDate()
    setTaskPaintFlags()
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
    val cursor = DBHelper.getDB(context).rawQuery(query, null)
    changeCursor(cursor)
    notifyDataSetChanged()
  }

  def refresh() = {
    filter(currentQuery.toSQL)
  }

}
