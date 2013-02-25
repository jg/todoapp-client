package com.android.todoapp

object PostponePeriod extends Enumeration {
  type PostponePeriodValue = Value
  val NotSet, Hour, FourHours, TwelveHours, Day = Value

  def apply(s: String) = {
    new PostponePeriod(
      try {
        val normalized = s(0).toUpperCase + s.substring(1, s.length).toLowerCase()
        withName(normalized)
      } catch  {
        case e: java.util.NoSuchElementException => PostponePeriod.NotSet
      }
    )
  }
  implicit def priority2string(d: PostponePeriod): String = d.toString

  def stringValues: Array[String] = values.toArray.map(_.toString)
}


import PostponePeriod._
class PostponePeriod(val name: PostponePeriodValue) {
  override def toString = name.toString

  override def equals(that: Any): Boolean = that match {
    case that: PostponePeriodValue => name == that
    case that: PostponePeriod => this == that
    case _ => false
  }

  def toInt = name match {
    case Hour => 60
    case FourHours => 4*60
    case TwelveHours => 12*60
    case Day => 24*60
  }
}
