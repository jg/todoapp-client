package com.android.todoapp;

import android.app.AlertDialog
import android.content.DialogInterface.OnClickListener
import java.lang.CharSequence
import android.content.DialogInterface
import android.content.Context
import android.os.Bundle
import android.app.Dialog
import android.support.v4.app.DialogFragment

class PickerDialog(context: Context, options: Array[CharSequence], listener: (String) => Unit)
  extends DialogFragment with SelectionAccess[String]{

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder = new AlertDialog.Builder(context)
    builder.setItems(options, new DialogInterface.OnClickListener() {
      def onClick(dialog: DialogInterface, which: Int) = {
        setSelection(options(which).toString)
        listener(options(which).toString())
      }
    })

    builder.create()
  }

}
