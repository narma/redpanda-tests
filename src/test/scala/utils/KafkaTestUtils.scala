package utils

import cats.effect.{Blocker, Concurrent, ContextShift}
import fs2.kafka._
import org.apache.kafka.clients.admin.NewTopic

import scala.concurrent.duration._

object KafkaTestUtils {

  def createTopicsSeq[F[_]: ContextShift: Concurrent](
    blocker: Blocker,
    bootstrapServers: String,
    replicationFactor: Int = 1,
    partitionCount: Int = 6
  )(topics: Seq[String]): F[Unit] =
    createTopics(blocker, bootstrapServers, replicationFactor, partitionCount)(topics: _*)

  def createTopics[F[_]: ContextShift: Concurrent](
    blocker: Blocker,
    bootstrapServers: String,
    replicationFactor: Int = 1,
    partitionCount: Int = 6
  )(topics: String*): F[Unit] = {
    val settings = AdminClientSettings[F]
      .withBootstrapServers(bootstrapServers)
      .withBlocker(blocker)
      .withRequestTimeout(6.seconds)
      .withCloseTimeout(6.seconds)
      .withConnectionsMaxIdle(6.seconds)
      .withRetries(2)
      .withRetryBackoff(1.seconds)

    KafkaAdminClient.resource(settings).use { client =>
      val kafkaTopics = topics.map { topic =>
        new NewTopic(topic, partitionCount, replicationFactor.toShort)
      }
      client.createTopics(kafkaTopics)
    }
  }
}
