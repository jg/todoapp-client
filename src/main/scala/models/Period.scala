package com.android.todoapp

class Period
case class PeriodNotSet extends Period { override def toString = "Not Set" }
case class EveryDay extends Period { override def toString = "Every Day" }
case class EveryWeek extends Period { override def toString = "Every Week" }
case class EveryTwoWeeks extends Period { override def toString = "Every Two Weeks" }
case class EveryMonth extends Period { override def toString = "Every Month" }
case class EveryYear extends Period { override def toString = "Every Year" }
case class AfterADay extends Period { override def toString = "After A Day" }
case class AfterAWeek extends Period { override def toString = "After A Week" }
case class AfterTwoWeeks extends Period { override def toString = "After Two Weeks" }
case class AfterAMonth extends Period { override def toString = "After A Month" }
case class AfterAYear extends Period { override def toString = "After A Year" }

object Period {
  val values: List[Period] = List(PeriodNotSet(), EveryDay(), EveryWeek(), EveryTwoWeeks(), EveryMonth(), EveryYear(), AfterADay(), AfterAWeek(), AfterTwoWeeks(), AfterAMonth(), AfterAYear())

  def fromString(label: String): Period = values.find(_.toString == label) match {
    case Some(caseClass) => caseClass
    case None => PeriodNotSet()
  }

  def apply(s: String) = fromString(s)

  def stringValues: Array[String] = values.map(_.toString).toArray[String]

}

