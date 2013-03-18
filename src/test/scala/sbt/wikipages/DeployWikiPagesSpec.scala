package sbt.wikipages

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FunSpec
import sbt._
import org.json4s.jackson.JsonMethods._

class DeployWikiPagesSpec extends FunSpec with ShouldMatchers {

  describe("deploy-wiki-pages") {

    for (
      (f, n, r) ← WikiPages.WikiPagesTasks.deployWikiPages(
        file("src/test/wiki"),
        "sbt-wikipages/test/1",
        "http://issuetrackerddit.afip.gov.ar/pruebas/login/jsonrpc",
        Seq(Credentials("Trac", "issuetrackerddit.afip.gov.ar", "pruebas", "pruebas")))
    ) {
      println("%s %s: %s" format (f, n, compact(render(r))))
    }

  }

}
