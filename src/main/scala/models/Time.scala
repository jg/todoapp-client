package com.android.todoapp

object Time {
  def apply(hour: Int, minutes: Int) = new Time(hour, minutes)
  implicit def time2Integer(d: Time): Int = d.minutes

  def fromMinutes(m: Int) = new Time(m/60, m%60)

  def parse(s: String) = {
    val lst = s.split(":").map(_.toInt)
    new Time(lst.first, lst.last)
  }
}

class Time(val hour: Int, val minutes: Int) {

  def toInt = hour*60+minutes

  override def toString = {
    List(hour, minutes).map(x => String.format("%02d", int2Integer(x))).mkString(":")
  }

}
