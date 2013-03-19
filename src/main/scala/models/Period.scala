package com.android.todoapp

abstract class Period {
  def amount: Integer
  def isEqual(startDate: Date, date: Date): Boolean
}
case object SixHours extends Period {
  override def toString = "Six Hours"
  def amount = 6
  def isEqual(startDate: Date, date: Date) = 
    startDate.hourDifference(date) <= 6
}
case object Day extends Period {
  override def toString = "A Day"
  def amount = 24
  def isEqual(startDate: Date, date: Date) =
    startDate.YYYYMMDD == date.YYYYMMDD
}
case object Week extends Period {
  override def toString = "A Week"
  def amount = 24*7
  def isEqual(startDate: Date, date: Date) =
    startDate.week == date.week
}
case object TwoWeeks extends Period {
  override def toString = "Two Weeks"
  def amount = 24*7*2
  def isEqual(startDate: Date, date: Date) =
    startDate.week   == date.week || startDate.week+1 == date.week
}
case object Month extends Period {
  override def toString = "A Month"
  def amount = 30*24
  def isEqual(startDate: Date, date: Date) =
    startDate.month == date.month
}

abstract class RepeatPattern

case class RepeatAfter(period: Period) extends RepeatPattern {
  override def toString = "After " + period.toString
}
case class RepeatEvery(period: Period) extends RepeatPattern {
  override def toString = "Every " + period.toString
  def isNextPeriod(startDate: Date, date: Date) =
    !period.isEqual(startDate, date)
}

/*
case object PeriodNotSet extends Period {
  override def toString = "Not Set"
  def amount = 0
}
*/


object RepeatPattern {
  val periods = List(SixHours, Day, Week, TwoWeeks, Month)
  val patterns = periods.map(RepeatAfter(_)) ++ periods.map(RepeatEvery(_))
  val values: List[String] = List("Not Set") ++ patterns.map(_.toString)

  def fromString(label: String): Option[RepeatPattern] = patterns.find(_.toString == label)
  def apply(s: String) = fromString(s)
  def stringValues: Array[String] = values.toArray[String]
}

