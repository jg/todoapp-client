package com.android.todoapp;

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.{CheckBox, Checkable, CheckedTextView, RelativeLayout}

class TaskLayout(context: Context, attrs: AttributeSet)
  extends RelativeLayout(context, attrs) with Checkable {
  private var checkbox: CheckBox = _

    override def onFinishInflate(): Unit = {
      super.onFinishInflate();

      for (i <- 0 to getChildCount()) {
        val v = getChildAt(i)
        if (v.isInstanceOf[CheckBox])
          checkbox = v.asInstanceOf[CheckBox];
      }
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
