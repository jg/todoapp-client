package com.android.todoapp

trait DBModel {
  def tableCreateStatement: String
  def tableDropStatement: String
}
