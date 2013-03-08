package com.android.todoapp

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods._
import org.apache.http.client.ResponseHandler
import org.apache.http.{HttpEntity, HttpResponse}
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import org.apache.http.util.EntityUtils
import org.apache.http.HttpException
import org.apache.http.entity.StringEntity
import org.apache.http.protocol.HTTP
import java.io.IOException
import java.net.UnknownHostException
import java.lang.StringBuilder
import java.io.{InputStreamReader, BufferedReader, InputStream}

object HttpAgent {
  val TAG = "ApiClient"

  var statusCode: Int = _

  def get(url: String, username: String, password: String): String = {
    val httpclient = new DefaultHttpClient()
    try {
      httpclient.getCredentialsProvider().setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(username, password))

      val httpget     = new HttpGet(url)
      val requestLine = httpget.getRequestLine().toString()
      Log.i("executing request: " + requestLine)
      val response    = httpclient.execute(httpget)
      statusCode      = response.getStatusLine().getStatusCode()

      Log.i("response status: " + Integer.toString(statusCode))

      val entity = response.getEntity()
      val s      = entity.getContent()

      inputStreamToString(s)
    } finally {
      httpclient.getConnectionManager().shutdown()
    }
  }

  def postJSON(url: String, username: String, password: String, json: String): String = {
    val httpclient = new DefaultHttpClient()
    try {
      httpclient.getCredentialsProvider().setCredentials(
        AuthScope.ANY,
        new UsernamePasswordCredentials(username, password))

      val entity = new StringEntity(json, HTTP.UTF_8)
      entity.setContentType("application/json")

      val httpPost     = new HttpPost(url)
      httpPost.setEntity(entity)
      val requestLine = httpPost.getRequestLine().toString()
      // Log.i("executing request: " + requestLine)
      val response    = httpclient.execute(httpPost)
      statusCode      = response.getStatusLine().getStatusCode()

      // Log.i("response status: " + Integer.toString(statusCode))

      val responseEntity = response.getEntity()
      val s      = responseEntity.getContent()


      val responseString = inputStreamToString(s)

      // Log.i("json: " + json)
      // Log.i("response: " + responseString)
      responseString
    } finally {
      httpclient.getConnectionManager().shutdown()
    }
  }

  private def inputStreamToString(s: InputStream): String = {
    @throws(classOf[IOException])
    val r = new BufferedReader(new InputStreamReader(s))
    val total = new StringBuilder()

    var line = ""
    line = r.readLine()
    while (line != null ) {
      total.append(line)
      line = r.readLine()
    }

    total.toString()
  }
}
