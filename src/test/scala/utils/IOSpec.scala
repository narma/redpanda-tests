package utils

import cats.effect.{ConcurrentEffect, ContextShift, IO, Timer}
import fs2.Stream
import org.scalatest.{Assertion, AsyncTestSuite}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.{FiniteDuration, SECONDS}

trait IOSpec extends EffSpec[IO] { self: AsyncTestSuite =>
  def ioTimeout: FiniteDuration = FiniteDuration(60, SECONDS)

  implicit def contextShift: ContextShift[IO] = IO.contextShift(ExecutionContext.global)
  implicit def Eff: ConcurrentEffect[IO]      = IO.ioConcurrentEffect
  implicit def timer: Timer[IO]               = IO.timer(ExecutionContext.global)

  override implicit def effectToFutureAssertion(eff: IO[Assertion]): Future[Assertion] =
    eff.timeout(ioTimeout).unsafeToFuture()

  override implicit def streamToFutureAssertion(stream: Stream[IO, Assertion]): Future[Assertion] =
    stream.timeout(ioTimeout).compile.lastOrError.unsafeToFuture()
}
