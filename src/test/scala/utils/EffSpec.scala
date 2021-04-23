package utils

import cats.effect.Effect
import fs2.Stream
import org.scalatest.enablers.WheneverAsserting
import org.scalatest.{Assertion, AsyncTestSuite}

import scala.concurrent.Future

trait EffSpec[F[_]] { self: AsyncTestSuite =>
  implicit def Eff: Effect[F]

  implicit def effWheneverAsserting: WheneverAsserting[F[Assertion]] { type Result = F[Assertion] } =
    new EffectWheneverAsserting[F]
  implicit def effectToFutureAssertion(eff: F[Assertion]): Future[Assertion] =
    Eff.toIO(eff).unsafeToFuture()

  implicit def streamToFutureAssertion(stream: Stream[F, Assertion]): Future[Assertion] =
    effectToFutureAssertion(stream.compile.lastOrError)
}
