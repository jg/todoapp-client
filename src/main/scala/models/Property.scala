package com.android.todoapp

import android.database.Cursor
import android.content.ContentValues
import android.content.Context

// TODO: addToContentValues could be generalized
trait PropertySerializer[T] {
  def sqlType: String = "string"

  def serialize(x: T): String

  def fromCursor(c: Cursor, pos: Integer): Option[T]

  def addToContentValues(c: ContentValues, name: String, value: T) 
}

// TODO: serializers should log unsuccessfull fromCursor calls
object TypeSerializers { 
  implicit object DatePropertySerializer extends PropertySerializer[Date] {
    def fromCursor(c: Cursor, pos: Integer): Option[Date] = try {
      Some(Date(c.getString(pos)))
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: Date) =
      c.put(name, value.completeFormat)

    def serialize(x: Date): String = x.completeFormat
  }

  implicit object LongPropertySerializer extends PropertySerializer[Long] {
    override def sqlType = "integer"

    def fromCursor(c: Cursor, pos: Integer): Option[Long] = try {
      Some(c.getLong(pos))
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: Long) =
      c.put(name, value: java.lang.Long)

    def serialize(x: Long): String = x.toString
  }

  implicit object StringPropertySerializer extends PropertySerializer[String] {
    def fromCursor(c: Cursor, pos: Integer): Option[String] = try {
      Some(c.getString(pos))
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: String) =
      c.put(name, value)

    def serialize(x: String): String = x
  }

  implicit object TimePropertySerializer extends PropertySerializer[Time] {
    override def sqlType = "integer"

    def fromCursor(c: Cursor, pos: Integer): Option[Time] = try {
      Some(Time.fromMinutes(c.getInt(pos)))
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: Time) =
      c.put(name, value.toInt: Integer)

    def serialize(x: Time): String = x.toInt.toString
  }

  implicit object PriorityPropertySerializer extends PropertySerializer[Priority] {
    def fromCursor(c: Cursor, pos: Integer): Option[Priority] = try {
      Some(Priority(c.getString(pos)))
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: Priority) =
      c.put(name, value.serialize: Integer)

    def serialize(x: Priority): String = x.toString
  }

  implicit object RepeatPropertySerializer extends PropertySerializer[RepeatPattern] { 
    // TODO: .get here looks weird, RepeatPattern should just throw an exception if someone provided garbage to the factory
    def fromCursor(c: Cursor, pos: Integer): Option[RepeatPattern] = try {
      Some(RepeatPattern(c.getString(pos)).get)
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: RepeatPattern) =
      c.put(name, value.toString)

    def serialize(x: RepeatPattern): String = x.toString
  }

  implicit object PeriodPropertySerializer extends PropertySerializer[Period] { 
    // TODO: .get here looks weird, Period should just throw an exception if someone provided garbage to the factory
    def fromCursor(c: Cursor, pos: Integer): Option[Period] = try {
      Some(Period(c.getString(pos)).get)
    } catch {
      case e: Exception => None
    }

    def addToContentValues(c: ContentValues, name: String, value: Period) =
      c.put(name, value.toString)

    def serialize(x: Period): String = x.toString
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

  // TODO: int types should not be quoted
  def jsonKeyValue: String = {
    if (value.isDefined)
      name + ": " + "\"" + propertySerializer.serialize(value.get) + "\""
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

  // set property at position pos in cursor if deserialization succeeded
  def setFromCursor(c: Cursor, pos: Integer) = propertySerializer.fromCursor(c, pos) match {
    case Some(value) => set(value)
    case _ => ()
  }

  def registerOnSetCallback(f: OnSetCallback)(implicit context: Context) = onSetCallback = Some(f)
}
