package com.android.todoapp;

trait SelectionAccess[T] {
  var selection: Option[T] = None

  def setSelection(value: T) = selection = Some(value)

  def hasSelection = !selection.isEmpty
}

