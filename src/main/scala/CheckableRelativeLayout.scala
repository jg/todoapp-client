package com.android.todoapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RelativeLayout;

class CheckableRelativeLayout(context: Context, attrs: AttributeSet)
  extends RelativeLayout(context, attrs) with Checkable {
  private var checkbox: CheckedTextView = _;

    override def onFinishInflate(): Unit = {
      super.onFinishInflate();

      for (i <- 0 to getChildCount()) {
        val v = getChildAt(i)
        if (v.isInstanceOf[CheckedTextView])
          checkbox = v.asInstanceOf[CheckedTextView];
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
