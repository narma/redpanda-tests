package app

import cats.effect.IO
import com.dimafeng.testcontainers.KafkaContainer

class KafkaSpec extends AppTests {
  override val container                      = KafkaContainer()
  override def ioBootstrapServers: IO[String] = IO(container.bootstrapServers)
}
