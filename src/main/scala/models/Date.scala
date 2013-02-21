package com.android.todoapp

import org.scala_tools.time.Imports._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTimeConstants

object Today { def unapply(s: String): Boolean = s.matches("[Tt]oday") }
object Tomorrow { def unapply(s: String): Boolean = s.matches("[Tt]omorrow") }
object Monday { def unapply(s: String): Boolean = s.matches("[Mm]onday") }
object Tuesday { def unapply(s: String): Boolean = s.matches("[Tt]uesday") }
object Wednesday { def unapply(s: String): Boolean = s.matches("[Ww]ednesday") }
object Thursday { def unapply(s: String): Boolean = s.matches("[tT]hursday") }
object Friday { def unapply(s: String): Boolean = s.matches("[fF]riday") }
object Saturday { def unapply(s: String): Boolean = s.matches("[sS]aturday") }
object Sunday { def unapply(s: String): Boolean = s.matches("[sS]unday") }
object StandardFormat {
  def unapply(s: String): Option[Date] = {
    try {
      Some(new Date(DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(s)))
    } catch {
      case ex: IllegalArgumentException => None
    }
  }
}

object MilisFormat {
  def unapply(millis: Long): Option[Date] = {
    try {
      Some(Date.fromMillis(millis))
    } catch {
      case ex: IllegalArgumentException => None
    }
  }
}

object Date {
  implicit def date2long(d: Date): java.lang.Long = d.getMillis

  def apply(s: String) = parse(s)

  def parse(s: String): Date = s match {
    case Today()     => new Date(new DateTime())
    case Tomorrow()  => new Date(new DateTime()+1.day)
    case Monday()    => Date.monday
    case Tuesday()   => Date.tuesday
    case Wednesday() => Date.wednesday
    case Thursday()  => Date.thursday
    case Friday()    => Date.friday
    case Saturday()  => Date.saturday
    case Sunday()    => Date.sunday
    case StandardFormat(date) => date
    case _ => throw new RuntimeException("Could not parse date: " + s)
  }

  def fromMillis(millis: Long) = new Date(new DateTime(millis))

  def fromToday(n: Int) = new Date(new DateTime() + n.days)

  def today = fromToday(0)

  def tomorrow = fromToday(1)

  def dayOfWeek(weekday: Int) = {
    val today = new DateTime()
    if (today > today.withDayOfWeek(weekday)) {
      // we're already past the given weekday in the week
      val week = today.weekOfWeekyear().get()
      new Date(today.withWeekOfWeekyear(week+1).withDayOfWeek(weekday))
    } else new Date(today.withDayOfWeek(weekday))
  }

  def monday    = dayOfWeek(DateTimeConstants.MONDAY)
  def tuesday   = dayOfWeek(DateTimeConstants.TUESDAY)
  def wednesday = dayOfWeek(DateTimeConstants.WEDNESDAY)
  def thursday  = dayOfWeek(DateTimeConstants.THURSDAY)
  def friday    = dayOfWeek(DateTimeConstants.FRIDAY)
  def saturday  = dayOfWeek(DateTimeConstants.SATURDAY)
  def sunday    = dayOfWeek(DateTimeConstants.SUNDAY)

  def now = new Date(new DateTime())

}


class Date(date: DateTime) {
  override def toString = DateTimeFormat.forPattern("yyyy-MM-dd").print(date)

  def fullFormat = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm").print(date)

  def dayMonthFormat = DateTimeFormat.forPattern("dd MMM").print(date)

  def weekday = DateTimeFormat.forPattern("EEEE").print(date).toLowerCase

  def isToday = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    fmt.print(date) == fmt.print(new DateTime())
  }

  def isTomorrow = {
    val fmt = DateTimeFormat.forPattern("yyyy-MM-dd")
    fmt.print(date) == fmt.print(new DateTime() + 1.day)
  }

  def getMillis = date.getMillis()

}