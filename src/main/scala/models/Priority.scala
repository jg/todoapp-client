package com.android.todoapp

import scala.collection.mutable.LinkedHashMap


object Priority extends Enumeration {
  type PriorityValue = Value
  val High, Low, Normal = Value

  def apply(s: String) = {
    new Priority(
      try {
        val normalized = s(0).toUpperCase + s.substring(1, s.length).toLowerCase()
        withName(normalized)
      } catch  {
        case e: java.util.NoSuchElementException => Priority.Normal
      }
    )
  }
  implicit def priority2string(d: Priority): String = d.toString

  def stringValues: Array[String] = values.toArray.map(_.toString)

  def deserialize(priority: Int): Priority = priority match {
    case 1 => new Priority(Priority.High)
    case -1 => new Priority(Priority.Low)
    case 0 => new Priority(Priority.Normal)
  }
}


import Priority._
class Priority(val value: PriorityValue) {
  override def toString = value.toString

  override def equals(that: Any): Boolean = that match {
    case that: PriorityValue => value == that
    case that: Priority => this == that
    case _ => false
  }

  def serialize = value match {
    case Priority.High => 1
    case Priority.Low => -1
    case _ => 0
  }

}


