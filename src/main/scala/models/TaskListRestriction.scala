package com.android.todoapp

import scala.collection.mutable.Queue

abstract class TaskListRestriction

case class TaskListFilter extends TaskListRestriction

case object FilterToday extends TaskListFilter {
  override def toString = "Today"
}

case object FilterThisWeek extends TaskListFilter {
  override def toString = "Week"
}


