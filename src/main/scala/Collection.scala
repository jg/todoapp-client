package com.android.todoapp

import scala.collection.mutable.ListBuffer

class JSONTokener(json: String) extends org.json.JSONTokener(json)
class JSONException(s: String) extends org.json.JSONException(s)

class JSONObject(jsonObject: org.json.JSONObject) {
  implicit def extendJSONObject(x: org.json.JSONObject): JSONObject  = new JSONObject(x)
  implicit def extendJSONArray(x: org.json.JSONArray): JSONArray  = new JSONArray(x)

  def getJSONObject(name: String): JSONObject = jsonObject.getJSONObject(name)

  def getJSONArray(name: String): Option[JSONArray] = {
    try {
      Some(jsonObject.getJSONArray(name))
    } catch {
      case e: JSONException => None
    }
  }

  def getString(name: String): Option[String] = {
    try {
      Some(jsonObject.getString(name))
    } catch {
      case e: JSONException => None
    }
  }
}

class JSONArray(jsonArray: org.json.JSONArray) {
  implicit def extendJSONArray(x: org.json.JSONObject): JSONObject  = new JSONObject(x)
  def length = jsonArray.length

  def getJSONObject(i: Int): JSONObject = jsonArray.getJSONObject(i)

  def toList[A](f: (JSONObject) => A): List[A] =
    (for (i <- Range(0, jsonArray.length)) yield f(jsonArray.getJSONObject(i))).toList
}

case class Link(rel: String, href: String)
case class Data(name: String, value: Option[String], prompt: Option[String])
case class Item(href: String, data: Option[List[Data]], links: Option[List[Link]])
case class Query(href: String, rel: String, name: Option[String], prompt: Option[String], data: Option[List[Data]])
// case class Query(href: String, data: List[Data], links: List[Link])

object Collection {
  def apply(json: String) = new Collection(json)
  def apply(uri: String, username: String, password: String) =
    new Collection(HttpAgent.get(uri, username, password))
}

/**
 * Refer to http://amundsen.com/media-types/collection/format/ for reference on
 * collection+json format
 */
class Collection(json: String) {
  def jsonObject: JSONObject = new JSONTokener(json).nextValue().asInstanceOf[JSONObject]

  def collection: JSONObject = jsonObject.getJSONObject("collection")

  def links: Option[List[Link]] =
    for (links <- collection.getJSONArray("links"))
      yield links.toList[Link](jsonObjectToLink)

  def items: Option[List[Item]] = {
    for (items <- collection.getJSONArray("items"))
      yield items.toList[Item](jsonObjectToItem)
  }

  def queries: Option[List[Query]] = {
    for (queries <- collection.getJSONArray("queries"))
      yield queries.toList[Query](jsonObjectToQuery)
  }

  def jsonObjectToLink(x: JSONObject): Link = Link(x.getString("rel").get, x.getString("href").get)

  def jsonObjectToData(x: JSONObject): Data =
    Data(x.getString("name").get, x.getString("value"), x.getString("prompt"))

  def jsonObjectToItem(x: JSONObject): Item = {
    val data  = for (data <- x.getJSONArray("data")) yield data.toList[Data](jsonObjectToData)
    val links = for (links <- x.getJSONArray("links")) yield links.toList[Link](jsonObjectToLink)

    Item(x.getString("href").get, data, links)
  }

  def jsonObjectToQuery(x: JSONObject): Query = {
    val data  = for (data <- x.getJSONArray("data")) yield data.toList[Data](jsonObjectToData)
    Query(x.getString("href").get,
          x.getString("rel").get,
          x.getString("prompt"),
          x.getString("name"),
          data)
  }

}
