package com.android.todoapp

abstract class Period {}

// EachPeriod
abstract class EachPeriod extends Period {
  def amount: Integer // number of days in period
}
case object AfterSixHours extends EachPeriod {
  override def toString = "After Six Hours"
  def amount = 6
}
case object AfterADay extends EachPeriod {
  override def toString = "After A Day"
  def amount = 24
}
case object AfterAWeek extends EachPeriod {
  override def toString = "After A Week"
  def amount = 24*7
}
case object AfterTwoWeeks extends EachPeriod {
  override def toString = "After Two Weeks"
  def amount = 24*7*2
}
case object AfterAMonth extends EachPeriod {
  override def toString = "After A Month"
  def amount = 30*24
}

// EveryPeriod

abstract class EveryPeriod extends Period {
  def isEqual(statDate: Date, date: Date): Boolean
  def isNextPeriodDate(date: Date): Boolean
  def isNextPeriod(startDate: Date, date: Date) =
    !isEqual(startDate, date) && isNextPeriodDate(date)
}
case object EveryDay extends EveryPeriod {
  override def toString = "Every Day"
  def isEqual(startDate: Date, date: Date) =
    startDate.YYYYMMDD == date.YYYYMMDD
  def isNextPeriodDate(date: Date) = true
}
case object EveryWeek extends EveryPeriod {
  override def toString = "Every Week"
  def isEqual(startDate: Date, date: Date) =
    startDate.week == date.week
  def isNextPeriodDate(date: Date) =
    date.isStartOfWeek
}
case object EveryTwoWeeks extends EveryPeriod {
  override def toString = "Every Two Weeks"
  def isEqual(startDate: Date, date: Date) =
    startDate.week   == date.week || startDate.week+1 == date.week
  def isNextPeriodDate(date: Date) =
    date.isStartOfWeek
}
case object EveryMonth extends EveryPeriod {
  override def toString = "Every Month"
  def isEqual(startDate: Date, date: Date) =
    startDate.month == date.month
  def isNextPeriodDate(date: Date) =
    date.isStartOfMonth
}

case object PeriodNotSet extends Period {
  override def toString = "Not Set"
  def amount = 0
}


object Period {
  val values: List[Period] = List(PeriodNotSet, EveryDay, EveryWeek, EveryTwoWeeks, EveryMonth, AfterADay, AfterAWeek, AfterTwoWeeks, AfterAMonth)

  def fromString(label: String): Period = values.find(_.toString == label) match {
    case Some(caseClass) => caseClass
    case None => PeriodNotSet
  }

  def apply(s: String) = fromString(s)

  def stringValues: Array[String] = values.map(_.toString).toArray[String]

}

