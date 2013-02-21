package com.android.todoapp

import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.ResponseHandler
import org.apache.http.{HttpEntity, HttpResponse}
import org.apache.http.impl.client.{BasicResponseHandler, DefaultHttpClient}
import org.apache.http.util.EntityUtils
import org.apache.http.HttpException
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
      statusCode     = response.getStatusLine().getStatusCode()

      Log.i("response status: " + Integer.toString(statusCode))

      val entity = response.getEntity()
      val s      = entity.getContent()

      inputStreamToString(s)
    } catch {
      case e: UnknownHostException => {
        Log.i(e.toString())
        "Could not resolve host"
      }
      case e: IOException => {
        Log.i(e.toString())
        e.toString()
      }
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