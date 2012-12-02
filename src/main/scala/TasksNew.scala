package com.android.todoapp

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.content.Intent
import android.widget.Spinner
import android.widget.ArrayAdapter
import Implicits._

import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat

class TasksNew extends Activity with TypedActivity {
  override def onCreate(bundle: Bundle) {
    super.onCreate(bundle)
    setContentView(R.layout.tasks_new)

    initTaskPrioritySpinner()
    initDueDateSpinner()
    initTaskListSpinner()

  }

  // Button Handlers

  def saveTaskButtonHandler(view: View) {
    val task = new Task(findViewById(R.id.title).asInstanceOf[TextView])
    task.priority = mapPriorityToValue(getSpinnerValue(R.id.priority))
    Tasks.add(this, task)
    startActivity(new Intent(this, classOf[MainActivity]))
  }

  def mapPriorityToValue(priority: String): Int = priority match {
    case "high" => 1
    case "low" => -1
    case _ => 0
  }


  // Init Code

  def getSpinnerValue(id: Int): String = {
    findViewById(id).asInstanceOf[Spinner].getSelectedView().asInstanceOf[TextView].getText().toString()
  }

  def initTaskListSpinner() = initSpinnerFromResource(R.id.task_list, R.array.task_lists);

  def initTaskPrioritySpinner() = initSpinnerFromResource(R.id.priority, R.array.task_priorities);

  def initDueDateSpinner() {
    def weekday(date: Date): String = new SimpleDateFormat("EEEE").format(date)

    def today: Date = new Date()

    def addDays(date: Date, days: Integer): Date ={
      val c: Calendar = Calendar.getInstance()
      c.setTime(date)
      c.add(Calendar.DATE, days)
      c.getTime()
    }

    def labels: Array[String] = {
      val range = 6
      val dates = Range(0, range).map(days => addDays(today, days))
      val labels: List[String] = List("today", "tomorrow") ::: dates.drop(2).map(weekday(_)).toList

      labels.toArray[String]
    }

    initSpinnerFromArray(R.id.due_date, labels)
  }


  // Helper functions

  def initSpinnerFromResource(spinnerId: Integer, resource: Integer) {
    val spinner: Spinner = findViewById(spinnerId).asInstanceOf[Spinner];
    val adapter: ArrayAdapter[CharSequence] = ArrayAdapter.createFromResource(this,
          resource, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
  }

  def initSpinnerFromArray(spinnerId: Integer, array: Array[String]) {
    val adapter: ArrayAdapter[String] = new ArrayAdapter(this, android.R.layout.simple_spinner_item, array);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    val spinner: Spinner = findViewById(spinnerId).asInstanceOf[Spinner];
    spinner.setAdapter(adapter);
  }

}
