package com.android.todoapp;

import android.app.TimePickerDialog
import android.content.DialogInterface.OnClickListener
import java.lang.CharSequence
import android.content.DialogInterface
import android.content.Context
import android.os.Bundle
import android.app.Dialog
import android.support.v4.app.DialogFragment
import android.view.LayoutInflater
import android.widget.{CalendarView, Toast}
import android.widget.TimePicker
import java.util.Calendar
import android.text.format.DateFormat

class TimePickerDialog(context: Context, listener: (Int, Int) => Unit) extends DialogFragment
  with TimePickerDialog.OnTimeSetListener
  with SelectionAccess[String] {
    def handler = new TimePickerDialog.OnTimeSetListener {
      def onTimeSet(view: TimePicker, hour: Int, minute: Int) = {
        setSelection(hour.toString + ":" + minute.toString)
        listener(hour, minute)
      }
    }

    override def onCreateDialog(savedInstanceState: Bundle) = {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance();
        val (hour: Int, minute: Int) = (Calendar.HOUR_OF_DAY, Calendar.MINUTE)

        val dialog = new android.app.TimePickerDialog(context, handler, hour, minute, DateFormat.is24HourFormat(getActivity()));
        if (hasSelection) {
          val lst = selection.get.split(":") collect {case i:String => i.toInt}
          dialog.updateTime(lst.first, lst.last)
        }
        // TODO: it should grab focus on create

        dialog
    }

    def onTimeSet(view: TimePicker, hour: Int, minute: Int) = {
      setSelection(hour.toString + ":" + minute.toString)
      listener(hour, minute)
    }

}
