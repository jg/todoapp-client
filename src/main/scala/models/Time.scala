package com.android.todoapp

object Time {
  def apply(s: String) = new Time(s)

  def fromMinutes(minutes: Integer) = new Time((minutes/60).toString+":"+(minutes%60).toString)
  implicit def time2Integer(d: Time): Int = d.minutes
}

class Time(s: String) {
  lazy val minutes: Int = {
    val parts = s.split(":")
    parts(0).toInt*60 + parts(1).toInt
  }

  def toInt = minutes

  override def toString = s

}
