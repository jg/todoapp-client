package com.android.todoapp


object Priority {
  def apply(s: String) = new Priority(s)
  implicit def priority2string(d: Priority): String = d.name
  def fromInteger(i: Int) = new Priority(i match {
    case 1 => "high"
    case -1 => "low"
    case 0 => "normal"
  })
}

class Priority(val name: String) {
  lazy val value = name match {
    case "high" => 1
    case "low" => -1
    case _ => 0
  }

  def toInt = value

  override def toString = name
}
