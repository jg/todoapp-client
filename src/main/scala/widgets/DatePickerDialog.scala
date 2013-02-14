package com.android.todoapp;

import android.app.AlertDialog
import android.content.DialogInterface.OnClickListener
import java.lang.CharSequence
import android.content.DialogInterface
import android.content.Context
import android.os.Bundle
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.CalendarView

class DatePickerDialog(context: Context, prompt: String, listener: (String) => Unit)
  extends DialogFragment {
  var selection: Option[String] = None

  def inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder = new AlertDialog.Builder(context)
    val view = inflater.inflate(R.layout.date_picker, null)
    val calendarView = view.findViewById(R.id.calendar).asInstanceOf[CalendarView]
    builder.setView(view)
           .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
             override def onClick(dialog: DialogInterface, id: Int) {
               val date = Date.fromMillis(calendarView.getDate()).toString()
               selection = Some(date)
               listener(date)
             }
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               override def onClick(dialog: DialogInterface, id: Int) {
                 DatePickerDialog.this.getDialog().cancel();
               }
           })

    builder.create()
  }

}
