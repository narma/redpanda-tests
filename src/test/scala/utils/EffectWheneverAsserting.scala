package utils

import org.scalatest.Assertion
import org.scalatest.enablers.WheneverAsserting
import org.scalatest.exceptions.DiscardedEvaluationException

class EffectWheneverAsserting[F[_]] extends WheneverAsserting[F[Assertion]] {
  override type Result = F[Assertion]

  override def whenever(condition: Boolean)(fun: => F[Assertion]): F[Assertion] =
    if (condition) fun else throw new DiscardedEvaluationException
}
