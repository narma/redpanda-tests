import sbt._

object Dependencies {
  object V {
    val fs2kafka = "1.5.0"

    val scalaTest           = "3.2.2"
    val testcontainers      = "1.15.3"
    val testcontainersScala = "0.39.3"
    val airframeLog = "21.4.1"
    val slf4j = "1.7.30"

  }
  import V._

  val logDeps = Seq(
    "org.wvlet.airframe" %% "airframe-log" % airframeLog, "org.slf4j" % "slf4j-jdk14" % slf4j
  )

  val testDeps = Seq(
    "org.scalatest"     %% "scalatest"                  % scalaTest,
    "org.testcontainers" % "testcontainers"             % testcontainers,
    "com.dimafeng"      %% "testcontainers-scala"       % testcontainersScala,
    "com.dimafeng"      %% "testcontainers-scala-kafka" % testcontainersScala
  ).map(_ % Test)

  val allDeps = Seq(
    "com.github.fd4s" %% "fs2-kafka" % fs2kafka
  ) ++ testDeps ++ logDeps
}
