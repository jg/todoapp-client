package com.android.todoapp

import android.database.Cursor
import android.content.ContentValues
import android.content.Context

// TODO: addToContentValues could be generalized
trait PropertySerializer[T] {
  def sqlType: String = "string"

  def serialize(x: T): String =
    x.toString

  def fromCursor(c: Cursor, pos: Integer): T

  def addToContentValues(c: ContentValues, name: String, value: T) 
}

object TypeSerializers { 
  implicit object DatePropertySerializer extends PropertySerializer[Date] {
    def fromCursor(c: Cursor, pos: Integer): Date = 
      Date(c.getString(pos))

    def addToContentValues(c: ContentValues, name: String, value: Date) =
      c.put(name, value.completeFormat)
  }

  implicit object LongPropertySerializer extends PropertySerializer[Long] {
    override def sqlType = "integer"

    def fromCursor(c: Cursor, pos: Integer): Long = 
      c.getLong(pos)

    def addToContentValues(c: ContentValues, name: String, value: Long) =
      c.put(name, value: java.lang.Long)
  }

  implicit object StringPropertySerializer extends PropertySerializer[String] {
    def fromCursor(c: Cursor, pos: Integer): String = 
      c.getString(pos)

    def addToContentValues(c: ContentValues, name: String, value: String) =
      c.put(name, value)
  }

  implicit object TimePropertySerializer extends PropertySerializer[Time] {
    override def sqlType = "integer"

    def fromCursor(c: Cursor, pos: Integer): Time = 
      Time.fromMinutes(c.getInt(pos))

    def addToContentValues(c: ContentValues, name: String, value: Time) =
      c.put(name, value.toInt: Integer)
  }

  implicit object PriorityPropertySerializer extends PropertySerializer[Priority] {
    def fromCursor(c: Cursor, pos: Integer): Priority = 
      Priority(c.getString(pos))

    def addToContentValues(c: ContentValues, name: String, value: Priority) =
      c.put(name, value.serialize: Integer)
  }

  implicit object RepeatPropertySerializer extends PropertySerializer[RepeatPattern] { 
    // TODO: .get here looks weird, RepeatPattern should just throw an exception if someone provided garbage to the factory
    def fromCursor(c: Cursor, pos: Integer): RepeatPattern = 
      RepeatPattern(c.getString(pos)).get

    def addToContentValues(c: ContentValues, name: String, value: RepeatPattern) =
      c.put(name, value.toString)
  }

  implicit object PeriodPropertySerializer extends PropertySerializer[Period] { 
    // TODO: .get here looks weird, Period should just throw an exception if someone provided garbage to the factory
    def fromCursor(c: Cursor, pos: Integer): Period = 
      Period(c.getString(pos)).get

    def addToContentValues(c: ContentValues, name: String, value: Period) =
      c.put(name, value.toString)
  }
}

object PropertyConversions {
  implicit def propertyToValueConversion[T](p: Property[T]): Option[T] = p.value
  implicit def StringToCharSequence(s: String): java.lang.CharSequence = s.asInstanceOf[java.lang.CharSequence]
}

object Property {
  // def deserialize(value: T): Property[T]

  def apply[T](name: String, value: Option[T])(implicit propertySerializer: PropertySerializer[T]) = new Property(name, value)
  

}

class Property[T](val name: String, var value: Option[T])(implicit propertySerializer: PropertySerializer[T]) {
  type OnSetCallback = (Option[T]) => Unit
  var onSetCallback: Option[OnSetCallback] = None

  def jsonKeyValue: String = {
    if (value.isDefined)
      name + ": " + propertySerializer.serialize(value.get)
    else
      name + ": \"null\""
  }

  def sqlType: String = propertySerializer.sqlType

  def set(x: T) = {
    value = Some(x)
    for (callback <- onSetCallback) callback(value)
  }

  def setFromAny(x: Any) = {
    val v: T = x.asInstanceOf[T]
    value = Some(v)
    for (callback <- onSetCallback) callback(value)
  }

  def setOpt(x: Option[T]) = {
    value = x
    for (callback <- onSetCallback) callback(value)
  }

  def isEmpty = value.isEmpty

  def isDefined = value.isDefined

  def get: T = value.get

  def reset() = value = None

  def foreach(f: (T) => Unit): Unit = value.foreach(f)

  def addToContentValues(c: ContentValues) = value match {
    case Some(v) => propertySerializer.addToContentValues(c, name, v)
    case None => c.putNull(name)
  }

  def setFromCursor(c: Cursor, pos: Integer) = set(propertySerializer.fromCursor(c, pos))

  def registerOnSetCallback(f: OnSetCallback)(implicit context: Context) = onSetCallback = Some(f)
}
