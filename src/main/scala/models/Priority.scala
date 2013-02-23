package com.android.todoapp

import scala.collection.mutable.LinkedHashMap

trait PriorityMap {
  val map = LinkedHashMap(
    "high"   -> 1,
    "low"    -> -1,
    "normal" -> 0
  )
}


object Priority extends PriorityMap {
  def apply(s: String) = new Priority(s)
  implicit def priority2string(d: Priority): String = d.name

  def fromInt(i: Int) = map.find(_._2==i) match {
    case Some(kv) => new Priority(kv._1)
    case None => new Priority("normal")
  }

  def default = new Priority("medium")
}

class Priority(val name: String) extends PriorityMap {
  lazy val value: Int = map.get(name) match {
    case Some(id) => id
    case None => 0
  }

  def toInt = value


  override def toString = name
}
