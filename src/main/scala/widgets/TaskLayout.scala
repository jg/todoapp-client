package com.android.todoapp;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.{CheckBox, Checkable, CheckedTextView, RelativeLayout, LinearLayout}

class TaskLayout(context: Context, attrs: AttributeSet)
  extends LinearLayout(context, attrs) with Checkable {
  lazy val checkbox: CheckBox = findViewById(R.id.taskCheckbox).asInstanceOf[CheckBox]

    override def onFinishInflate(): Unit = {
      super.onFinishInflate();
    }

    override def isChecked(): Boolean = {
      if (checkbox != null)
        checkbox.isChecked()
      else
        false
    }

    override def setChecked(checked: Boolean) =
      if (checkbox != null) checkbox.setChecked(checked)

    override def toggle() =
      if (checkbox != null) checkbox.toggle();
}
