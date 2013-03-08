package com.android.todoapp

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
      case e: org.json.JSONException => None
    }
  }

  def getString(name: String): Option[String] = {
    try {
      Some(jsonObject.getString(name))
    } catch {
      case e: org.json.JSONException => None
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

