package com.android.todoapp

import android.widget.TextView

object Implicits {
  implicit def textViewToString(tv: TextView):String = tv.getText.toString
}
