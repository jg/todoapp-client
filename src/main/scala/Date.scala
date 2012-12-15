package com.android.todoapp

import org.scala_tools.time.Imports._
import org.joda.time.format.DateTimeFormat
import org.joda.time.DateTimeConstants

object Today { def unapply(s: String): Boolean = s.matches("[Tt]oday") }
object Tomorrow { def unapply(s: String): Boolean = s.matches("[Tt]morrow") }
object Monday { def unapply(s: String): Boolean = s.matches("[Mm]onday") }
object Tuesday { def unapply(s: String): Boolean = s.matches("[Tt]uesday") }
object Wednesday { def unapply(s: String): Boolean = s.matches("[Ww]ednesday") }
object Thursday { def unapply(s: String): Boolean = s.matches("[tT]hursday") }
object Friday { def unapply(s: String): Boolean = s.matches("[fF]riday") }
object Saturday { def unapply(s: String): Boolean = s.matches("[sS]aturday") }
object Sunday { def unapply(s: String): Boolean = s.matches("[sS]unday") }

object Date {
  def parse(s: String) = s match {
    case Today()     => Date.today
    case Tomorrow()  => Date.fromToday(1)
    case Monday()    => Date.monday
    case Tuesday()   => Date.tuesday
    case Wednesday() => Date.wednesday
    case Thursday()  => Date.thursday
    case Friday()    => Date.friday
    case Saturday()  => Date.saturday
    case Sunday()    => Date.sunday
  }

  def fromToday(n: Int) = new DateTime() + n.days

  def today = fromToday(0)
  def tomorrow = fromToday(1)


  def dayOfWeek(weekday: Int) = {
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
}

class Date(date: DateTime) {
  override def toString = DateTimeFormat.forPattern("yyyy-MM-dd").print(date)
}
