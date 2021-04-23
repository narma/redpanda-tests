package utils

import cats.effect.{Blocker, IO}
import fs2.Stream
import org.scalatest.flatspec.AsyncFlatSpec
import wvlet.log.{LogFormatter, LogLevel, Logger}

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

trait AppSpec extends AsyncFlatSpec with IOSpec {
  val blocker: Blocker = Blocker.liftExecutionContext(ExecutionContext.fromExecutor(Executors.newCachedThreadPool()))
  val traceEnabled     = false

  implicit def toStreamFromIO[T](io: IO[T]): Stream[IO, T] = Stream.eval(io)

  def trace(logString: String): IO[Unit] = IO.whenA(traceEnabled)(IO(println(logString)))
  def prn(logString: String): IO[Unit]   = IO(println(logString))

  private lazy val _initLogging = {
    Logger.setDefaultLogLevel(LogLevel.INFO)
    Logger.scanLogLevels // scan log.properties
    Logger.setDefaultFormatter(LogFormatter.IntelliJLogFormatter)
  }

  _initLogging
}
