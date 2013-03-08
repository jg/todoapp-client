package com.android.todoapp

import scala.collection.mutable.ListBuffer

case class Link(rel: String, href: String)
case class Data(name: String, value: Option[String], prompt: Option[String])
case class Item(href: String, data: Option[List[Data]], links: Option[List[Link]])
case class Query(href: String, rel: String, name: Option[String], prompt: Option[String], data: Option[List[Data]])

object Collection {
  def apply(json: String) = new Collection(json)
  def apply(uri: String, username: String, password: String) = new Collection(HttpAgent.get(uri, username, password))

  def postJSON(url: String, username: String, password: String, json: String) = HttpAgent.postJSON(url, username, password, json)
}

/**
 * Refer to http://amundsen.com/media-types/collection/format/ for reference on
 * collection+json format
 */
class Collection(json: String) {
  def jsonObject: JSONObject = new JSONObject((new JSONTokener(json)).nextValue().asInstanceOf[org.json.JSONObject])

  def href = collection.getString("href").get

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

  def template: Option[List[Data]] = {
    val template = collection.getJSONObject("template")
    for {
      data <- template.getJSONArray("data")
    }
      yield data.toList[Data](jsonObjectToData)
  }

  implicit def jsonObjectToLink(x: JSONObject): Link = Link(x.getString("rel").get, x.getString("href").get)

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
