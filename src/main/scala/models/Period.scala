package com.android.todoapp

object Period extends Enumeration {
  type PeriodValue = Value
  val NotSet, EveryDay, EveryWeek, EveryTwoWeeks, EveryMonth, EveryYear, AfterADay, AfterAWeek, AfterTwoWeeks, AfterAMonth, AfterAYear = Value

  def capitalize(s: String) = { s(0).toUpperCase + s.substring(1, s.length).toLowerCase }

  def normalize(name: String) = {
    if (name.contains(" "))
      name.toLowerCase().split(" ").map(capitalize(_)).mkString("")
    else
      name
  }

  def apply(s: String) = {
    new Period(
      try {
        withName(normalize(s))
      } catch  {
        case e: java.util.NoSuchElementException => Period.NotSet
      }
    )
  }

  def stringValues: Array[String] = values.toArray.map(_.toString)
}

import Period._
class Period(val name: PeriodValue) {
  override def toString = name.toString

  override def equals(that: Any): Boolean = that match {
    case that: PeriodValue => name == that
    case that: Period => this == that
    case _ => false
  }
}
