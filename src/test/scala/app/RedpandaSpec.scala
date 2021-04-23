package app

import cats.effect.IO
import testcontainers.RedpandaContainer

class RedpandaSpec extends AppTests {

  override val container                      = RedpandaContainer()
  override def ioBootstrapServers: IO[String] = IO(container.bootstrapServers)
}
