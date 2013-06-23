package com.android.todoapp

abstract class Period {
  def amount: Integer // seconds
  def isEqual(startDate: Date, date: Date): Boolean
}
object Period {
  def values = List(OneMinute, TenSeconds, Hour, FourHours, SixHours, Day, Week, TwoWeeks, Month)
  def fromString(label: String): Option[Period] = values.find(_.toString == label)
  def apply(s: String) = fromString(s)
}
case object TenSeconds extends Period {
  override def toString = "Ten Seconds"
  def amount = 10
  def isEqual(startDate: Date, date: Date) = true
}
case object OneMinute extends Period {
  override def toString = "Minute"
  def amount = 60
  def isEqual(startDate: Date, date: Date) =
    startDate.secondDifference(date) <= 60
}
case object Hour extends Period {
  override def toString = "Hour"
  def amount = 60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.hourDifference(date) <= 1
}
case object FourHours extends Period {
  override def toString = "Four Hours"
  def amount = 4*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.hourDifference(date) <= 4
}
case object SixHours extends Period {
  override def toString = "Six Hours"
  def amount = 6*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.hourDifference(date) <= 6
}
case object Day extends Period {
  override def toString = "A Day"
  def amount = 24*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.YYYYMMDD == date.YYYYMMDD
}
case object Week extends Period {
  override def toString = "A Week"
  def amount = 24*7*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.week == date.week
}
case object TwoWeeks extends Period {
  override def toString = "Two Weeks"
  def amount = 24*7*2*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.week   == date.week || startDate.week+1 == date.week
}
case object Month extends Period {
  override def toString = "A Month"
  def amount = 30*24*60*60
  def isEqual(startDate: Date, date: Date) =
    startDate.month == date.month
}

case object NoRepeat extends RepeatPattern {
  override def toString = "Not Set"
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

object RepeatPattern {
  val periods = List(OneMinute, SixHours, Day, Week, TwoWeeks, Month)
  val patterns = periods.map(RepeatAfter(_)) ++ periods.map(RepeatEvery(_))
  val values: List[String] = List("Not Set") ++ patterns.map(_.toString)

  def fromString(label: String): RepeatPattern = patterns.find(_.toString == label) match {
    case Some(pattern) => pattern
    case None => NoRepeat
  }
  def apply(s: String) = fromString(s)
  def stringValues: Array[String] = values.toArray[String]
}

