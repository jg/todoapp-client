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
import android.view.View
import android.support.v4.app.FragmentTransaction
import android.support.v4.app.FragmentManager

class DatePickerDialog(context: Context, prompt: String, listener: (Date) => Unit)
  extends DialogFragment
  with SelectionAccess[Date] {
  var view: Option[View] = None

  def inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  def calendarView: Option[CalendarView] = view match {
    case Some(view) => Some(view.findViewById(R.id.calendar).asInstanceOf[CalendarView])
    case None => None
  }

  def setDate(date: Date) = for (cv <- calendarView) cv.setDate(date.getMillis)

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder = new AlertDialog.Builder(context)
    view = Some(inflater.inflate(R.layout.date_picker, null))
    if (hasSelection) setDate(selection.get)
    builder.setView(view.get)
           .setPositiveButton("Pick", new DialogInterface.OnClickListener() {
             override def onClick(dialog: DialogInterface, id: Int) {
              for (calendarView <- calendarView) {
                 val date = Date.fromMillis(calendarView.getDate())
                 setSelection(date)
                 listener(date)
               }
             }
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               override def onClick(dialog: DialogInterface, id: Int) {
                 DatePickerDialog.this.getDialog().cancel();
               }
           })

    builder.create()
  }
  override def show(fmgr: FragmentManager, tag: String) = {
    // remove previous dialog if present before showing new one
    val fragmentTransaction = fmgr.beginTransaction()
    val prev = fmgr.findFragmentByTag(tag)
    if (prev != null) fragmentTransaction.remove(prev)
    fragmentTransaction.addToBackStack(null)

    super.show(fmgr: FragmentManager, tag: String)
  }

}
