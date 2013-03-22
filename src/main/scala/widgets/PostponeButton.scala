package com.android.todoapp
import android.widget.Button
import android.view.View
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.android.todoapp.Implicits._
import java.lang.CharSequence

case class PostponeButton(context: Context, button: Button, fragmentManager: FragmentManager, taskListView: TaskListView) {
  button.setOnClickListener((v: View) => 
    postponePeriodSelectionDialog.show(fragmentManager, "postpone-selection")
  )

  lazy val postponePeriodSelectionDialog: PickerDialog = {
    val choices = List(TenSeconds, Hour, FourHours, SixHours, Day).map(_.toString).toArray[String]
    val listener = (selection: String) => {
      val postponePeriod = Period(selection).get
      val items = taskListView.checkedItems
      items.foreach((task: Task) => {
        task.setPostpone(postponePeriod)
        task.save(context)
      })
      Util.pr(context, "Postponed " + items.size + " tasks")
    }
    new PickerDialog(context, choices.asInstanceOf[Array[CharSequence]], listener)
  }

}
