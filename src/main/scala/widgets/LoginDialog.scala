package com.android.todoapp

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
import android.widget.EditText

class LoginDialog(context: Context, listener: (Credentials) => Unit)
  extends DialogFragment {
  var view: Option[View] = None

  def inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]

  def username: String = view.get.findViewById(R.id.username).asInstanceOf[EditText].getText().toString

  def password: String = view.get.findViewById(R.id.password).asInstanceOf[EditText].getText().toString

  override def onCreateDialog(savedInstanceState: Bundle): Dialog = {
    val builder = new AlertDialog.Builder(context)
    view = Some(inflater.inflate(R.layout.login_dialog, null))

    builder.setView(view.get)
           .setPositiveButton("Login", new DialogInterface.OnClickListener() {
             override def onClick(dialog: DialogInterface, id: Int) =
              listener(Credentials(username, password))
           })
           .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               override def onClick(dialog: DialogInterface, id: Int) {
                 LoginDialog.this.getDialog().cancel();
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
