package utils

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import fs2.Stream
import org.scalatest.Assertion
import org.scalatest.flatspec.AsyncFlatSpec
import wvlet.log.{LogFormatter, LogLevel, Logger}

import scala.concurrent.Future
import scala.concurrent.duration._

trait AppSpec extends AsyncFlatSpec with AsyncIOSpec {
  val traceEnabled     = false
  val ioTimeout: FiniteDuration = 30.seconds

  implicit def toStreamFromIO[T](io: IO[T]): Stream[IO, T] = Stream.eval(io)

  override implicit def ioToFutureAssertion(io: IO[Assertion]): Future[Assertion] =
    io.timeout(ioTimeout).unsafeToFuture()

  implicit def streamToFutureAssertion(stream: Stream[IO, Assertion]): Future[Assertion] =
    stream.timeout(ioTimeout).compile.lastOrError.unsafeToFuture()


  def trace(logString: String): IO[Unit] = IO.whenA(traceEnabled)(IO.println(logString))
  def prn(logString: String): IO[Unit]   = IO.println(logString)

  private lazy val _initLogging = {
    Logger.setDefaultLogLevel(LogLevel.INFO)
    Logger.scanLogLevels // scan log.properties
    Logger.setDefaultFormatter(LogFormatter.IntelliJLogFormatter)
  }

  _initLogging
}
