import Dependencies._
import BuildHelper._

ThisBuild / scalaVersion := "2.13.5"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "redpandaTests",
    libraryDependencies ++= allDeps,
    fork := true,
    scalacOptions := appScalacOptions,
    excludeDependencies ++= Seq(
      // commons-logging is replaced by jcl-over-slf4j
      ExclusionRule("ch.qos.logback", "logback-classic"),
      ExclusionRule("org.slf4j", "slf4j-log4j12"),
      ExclusionRule("org.slf4j", "jul-to-slf4j")
    ),
    welcomeMessage,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
