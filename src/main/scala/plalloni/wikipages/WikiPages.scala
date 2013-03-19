package plalloni.wikipages

import java.nio.charset.Charset

import org.json4s._
import JsonDSL._
import jackson.JsonMethods._

import com.ning.http.client.Response

import dispatch.{ url ⇒ durl, _ }
import sbt._
import Keys._

object WikiPages extends Plugin {

  object WikiPagesKeys {

    val wikiPages = SettingKey[File]("wiki-pages", "Location of wiki pages to deploy")

    val wikiPagesBase = SettingKey[String]("wiki-pages-base", "Base page name to root wiki pages")

    val wikiPagesRpcUrl = SettingKey[String]("wiki-pages-rpc-url", "URL of target Wiki-RPC API")

    val publishWikiPages = TaskKey[Seq[(File, String, JValue)]]("publish-wiki-pages", "Deploy wiki pages")

  }

  import WikiPagesKeys._

  val newSettings = Seq (
    wikiPages <<= baseDirectory(_ / "src/wiki"),
    publishWikiPages <<= (wikiPages, wikiPagesBase, wikiPagesRpcUrl, credentials, streams) map Wiki.publishPages
  )

}
