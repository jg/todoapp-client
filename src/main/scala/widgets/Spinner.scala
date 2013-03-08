package com.android.todoapp
import android.content.Context
import android.widget.ArrayAdapter
import android.util.AttributeSet
import android.widget.TextView
import java.util.Calendar

class Spinner(context: Context, attrs: AttributeSet) extends android.widget.Spinner(context, attrs) {
  def fromResource(resource: Int) = {
    val adapter: ArrayAdapter[CharSequence] =
      ArrayAdapter.createFromResource(context, resource, android.R.layout.simple_spinner_item)

    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    setAdapter(adapter)
  }

  def fromArray(array: Array[String]) {
    val adapter: ArrayAdapter[String] =
      new ArrayAdapter(context, android.R.layout.simple_spinner_item, array)
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

    setAdapter(adapter)
  }

  def value: String =
    getSelectedView().asInstanceOf[TextView].getText().toString()

  def asDueDateSpinner() = {
    def labels: Array[String] = {
      val range = 6
      val dates = Range(0, range).map(days => Date.fromToday(days))
      val labels: List[String] = List("today", "tomorrow") ::: dates.drop(2).map(_.weekday).toList

      labels.toArray[String]
    }

    fromArray(labels)
  }

  def setSelection(s: String): Unit = {
    val adapter = getAdapter()
    for (i <- 1 to (adapter.getCount()-1) if adapter.getItem(i).toString == s) {
      setSelection(i)
    }
  }
}

