package plalloni.wikipages

import java.nio.charset.Charset

import org.json4s._
import JsonDSL._
import jackson.JsonMethods._

import dispatch.{ url ⇒ durl, _ }

import sbt._
import Keys._

object Wiki {

  val UTF8 = Charset.forName("UTF-8")

  val UnderscorePage = """^(.*)/_\.wiki$""".r

  val Page = """^(.*)\.wiki$""".r

  def publishPages(pagesLocation: File, baseName: String, wikiUrl: String, credentials: Seq[Credentials], s: TaskStreams): Seq[(File, String, JValue)] = {

    val host = url(wikiUrl).getHost

    val auth = Credentials.forHost(credentials, host) getOrElse sys.error("Missing credentials for host %s" format host)

    val mappings = ((pagesLocation ** "*.wiki") x rebase(pagesLocation, baseName.replace("([^/])$", "$1/"))) map {
      case (f, UnderscorePage(name)) ⇒ f → name
      case (f, Page(name))           ⇒ f → name
    }

    val promises = Http.promise.all {
      for ((file, pageName) ← mappings) yield {
        s.log.info("publish page %s from file %s" format (pageName, file))
        val pageContent = IO.read(file, UTF8)
        val pageAttributes = JObject()
        val params = JArray(List(pageName, pageContent, pageAttributes))
        val rpc = compact(("method" → "wiki.putPage") ~ ("params" → params))
        s.log.debug("page %s: jsonrpc call %s" format (pageName, rpc))
        val request =
          durl(wikiUrl)
            .POST
            .as(auth.userName, auth.passwd)
            .setHeader("Content-Type", "application/json; charset=utf-8")
            .setBody(rpc.getBytes)
        Http(request OK as.json4s.Json) map { result ⇒
          s.log.debug("page %s: jsonrpc result %s" format (pageName, compact(result)))
          (file, pageName, result)
        }
      }
    }

    promises().toSeq

  }

}
