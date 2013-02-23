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

class TimePickerDialog(context: Context, listener: (Time) => Unit) extends DialogFragment
  with TimePickerDialog.OnTimeSetListener
  with SelectionAccess[Time] {
    var initialTime: Option[Time] = None
    var dialog: Option[android.app.TimePickerDialog] = None

    def handler = new TimePickerDialog.OnTimeSetListener {
      def onTimeSet(view: TimePicker, hour: Int, minute: Int) = {
        setSelection(Time(hour, minute))
        listener(Time(hour, minute))
      }
    }

    override def onCreateDialog(savedInstanceState: Bundle) = {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance();
        val (hour: Int, minute: Int) = (Calendar.HOUR_OF_DAY, Calendar.MINUTE)

        dialog = Some(new android.app.TimePickerDialog(context, handler, hour, minute, DateFormat.is24HourFormat(getActivity())))
        if (hasSelection) 
          dialog.get.updateTime(selection.get.hour, selection.get.minutes)
        else if (!initialTime.isEmpty) 
          dialog.get.updateTime(initialTime.get.hour, initialTime.get.minutes)
        dialog.get
    }

    def onTimeSet(view: TimePicker, hour: Int, minute: Int) = {
      setSelection(Time(hour, minute))
      listener(Time(hour, minute))
    }

    def setInitialTime(time: Time) = { initialTime = Some(time) }

}
