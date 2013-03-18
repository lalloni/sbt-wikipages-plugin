package sbt.wikipages

import sbt.{ url ⇒ surl, _ }
import Keys._
import dispatch.{ url ⇒ durl, _ }
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.JObject
import com.ning.http.client.Response
import scala.io.Source
import java.nio.charset.Charset
import org.json4s.JValue
import org.json4s.JObject
import org.json4s.JArray

object WikiPages extends Plugin {

  import WikiPagesKeys._

  object WikiPagesKeys {

    val wikiPages = SettingKey[File]("wiki-pages", "Location of wiki pages to deploy")

    val wikiPagesBase = SettingKey[String]("wiki-pages-base", "Base page name to root wiki pages")

    val wikiPagesRpcUrl = SettingKey[String]("wiki-pages-rpc-url", "URL of target Wiki-RPC API")

    val deployWikiPages = TaskKey[Seq[(File, String, JValue)]]("deploy-wiki-pages", "Deploy wiki pages")

  }

  object WikiPagesTasks {

    val UTF8 = Charset.forName("UTF-8")

    val UnderscorePage = """^(.*)/_\.wiki$""".r

    val Page = """^(.*)\.wiki$""".r

    val OK = Set(200, 201)

    def boolean(response: Response): Either[String, Boolean] = {
      val sc = response.getStatusCode
      val content = response.getResponseBody(UTF8.displayName)
      println("%s %s: %s" format (sc, response.getStatusText, content))
      if (OK(sc)) Right(content.toBoolean)
      else Left("%s %s" format (sc, response.getStatusText))
    }

    def deployWikiPages(pagesLocation: File, baseName: String, wikiUrl: String, credentials: Seq[Credentials]): Seq[(File, String, JValue)] = {

      val host = surl(wikiUrl).getHost

      val auth = Credentials.forHost(credentials, host) getOrElse sys.error("Missing credentials for host %s" format host)

      val mappings = ((pagesLocation ** "*.wiki") x rebase(pagesLocation, baseName.replace("([^/])$", "$1/"))) map {
        case (f, UnderscorePage(name)) ⇒ f → name
        case (f, Page(name))           ⇒ f → name
      }

      val promises = Http.promise.all {
        for ((file, page) ← mappings) yield {
          println("deploying %s as %s" format (file, page))
          val call = compact(("method" → "wiki.putPage") ~ ("params" → JArray(List(page, IO.read(file, UTF8), JObject()))))
          println("with json call %s" format call)
          val request =
            durl(wikiUrl)
              .POST
              .as(auth.userName, auth.passwd)
              .setHeader("Content-Type", "application/json; charset=utf-8")
              .setBody(call.getBytes)
          Http(request OK as.json4s.Json).map(result ⇒ (file, page, result))
        }
      }

      promises().toSeq

    }

  }

  override val settings = Seq (
    wikiPages := file("src/wiki"),
    deployWikiPages <<=
      (WikiPagesKeys.wikiPages, WikiPagesKeys.wikiPagesBase, WikiPagesKeys.wikiPagesRpcUrl, credentials)
      .map(WikiPagesTasks.deployWikiPages _)
  )

}
