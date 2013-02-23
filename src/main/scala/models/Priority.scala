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
}


import Priority._
class Priority(val name: PriorityValue) {
  override def toString = name.toString

  override def equals(that: Any): Boolean = that match {
    case that: PriorityValue => name == that
    case that: Priority => this == that
    case _ => false
  }
}


