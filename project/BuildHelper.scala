import sbt._
import Keys._

object BuildHelper {
  def welcomeMessage = onLoadMessage := {
    import scala.{Console => C}

    def logo(text: String): String = s"${C.GREEN}$text${C.RESET}"
    def item(text: String): String = s"${C.GREEN}> ${C.CYAN}$text${C.RESET}"

    s"""|${logo("Redpanda compatibility tests")}

        |Useful sbt tasks:
        |
        |${item("test")} - Run all tests both for Kafka and Redpanda
        |${item("testOnly app.KafkaSpec")} - Runs all tests only with Kafka container
        |${item("testOnly app.RedpandaSpec")} - Runs all tests only with Kafka container
        |${item("testOnly app.RedpandaSpec -- -z \"#2\"")} - Run only one test case which have #2 in name
        |""".stripMargin
  }

  def appScalacOptions = Seq(
    "-encoding",
    "utf-8",
    "-feature",
    "-unchecked",
    "-deprecation",
    "-Xlint:_,-missing-interpolator,-byname-implicit",
    "-Ywarn-unused",
    "-Ymacro-annotations",
    "-Yrangepos",
    "-Werror",
    "-explaintypes",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-Xfatal-warnings",
    "-Wconf:any:error"
  )
}
