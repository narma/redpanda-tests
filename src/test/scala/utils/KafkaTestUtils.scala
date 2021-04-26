package utils

import cats.effect.Async
import fs2.kafka._
import org.apache.kafka.clients.admin.NewTopic

import scala.concurrent.duration._

object KafkaTestUtils {

  def createTopicsSeq[F[_]: Async](
    bootstrapServers: String,
    replicationFactor: Int = 1,
    partitionCount: Int = 6
  )(topics: Seq[String]): F[Unit] =
    createTopics(bootstrapServers, replicationFactor, partitionCount)(topics: _*)

  def createTopics[F[_]: Async](
    bootstrapServers: String,
    replicationFactor: Int = 1,
    partitionCount: Int = 6
  )(topics: String*): F[Unit] = {
    val settings = AdminClientSettings(bootstrapServers)
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
