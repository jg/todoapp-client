package com.android.todoapp
import android.widget.Button
import android.view.View
import android.content.Context
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import com.android.todoapp.Implicits._

case class PostponeButton(context: Context, button: Button, fragmentManager: FragmentManager) {
  button.setOnClickListener((v: View) => 
    postponePeriodSelectionDialog.show(fragmentManager, "postpone-selection")
  )

  lazy val postponePeriodSelectionDialog: PickerDialog = {
    val choices = PostponePeriod.stringValues.asInstanceOf[Array[CharSequence]]
    val listener = (selection: String) => ()
    new PickerDialog(context, choices, listener)
  }

}
