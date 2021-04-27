package app

import cats.effect.{IO, Resource}
import com.dimafeng.testcontainers.ForEachTestContainer
import fs2.Stream
import fs2.kafka._
import org.apache.kafka.common.TopicPartition
import org.scalatest.matchers.must.Matchers
import utils.AppSpec
import utils.KafkaTestUtils._

import scala.concurrent.duration.DurationInt

trait AppTests extends AppSpec with Matchers with ForEachTestContainer {
  def ioBootstrapServers: IO[String]
  def streamBootstrapServers: Stream[IO, String] = Stream.eval(ioBootstrapServers)

  it should "#1 produce messages when all topic was created" in {
    for {
      bootstrapServers <- streamBootstrapServers
      recordsCount      = 2
      topicsCount       = 15
      producer         <- Stream.resource(mkProducer(bootstrapServers))
      _                <- createTopicsSeq[IO](bootstrapServers)(mkTopics(topicsCount))
      records           = mkRecords(topicsCount, recordsCount)
      result           <- push(records)(producer)
    } yield result.records.map(_._1) mustEqual records.records
  }

  // possible related issue https://github.com/vectorizedio/redpanda/issues/194
  it should "#2 produce messages when all topic was created in reverse order" in {
    for {
      bootstrapServers <- streamBootstrapServers
      producer         <- Stream.resource(mkProducer(bootstrapServers))
      topicsCount       = 10
      records = ProducerRecords(
                  (1 to topicsCount).reverse.map(n => mkRecord(s"$n", s"hello$n", topic = s"output$n")).toList
                )
      _ <- createTopicsSeq[IO](bootstrapServers, partitionCount = 31)(
             mkTopics(topicsCount)
           )
      produceResult <- producer.produce(records).flatMap(identity)
    } yield produceResult.records.map(_._1) mustEqual records.records
  }

  it should "#3 produce messages when no topic was created" in {
    for {
      bootstrapServers <- ioBootstrapServers
      records           = mkRecords(4)
      produceResult    <- mkProducer(bootstrapServers).use(push(records))
    } yield produceResult.records.map(_._1) mustEqual records.records
  }

  it should "#4 latest and begin offsets for new topic should be zero" in {
    for {
      bootstrapServers <- streamBootstrapServers
      consumerSettings = ConsumerSettings[IO, String, String]
                           .withBootstrapServers(bootstrapServers)
      consumer   <- KafkaConsumer.stream(consumerSettings)
      topicsCount = 1
      _          <- createTopicsSeq[IO](bootstrapServers, partitionCount = 500)(mkTopics(topicsCount))
      testTopic   = s"output$topicsCount"
      partitions <- consumer.partitionsFor(testTopic, 3.seconds)
      topicPartitions = partitions.map { p =>
                          new TopicPartition(p.topic(), p.partition())
                        }.toSet
      beginOffsets <- consumer.beginningOffsets(topicPartitions, 2.seconds)
      endOffsets   <- consumer.endOffsets(topicPartitions, 2.seconds)
    } yield {
      // Redpanda (current = v21.4.15) can return Long.MinValue offset for some partitions
      all(beginOffsets.values) must be(0L)
      beginOffsets mustEqual endOffsets
    }
  }

  def mkRecord(key: String, body: String, topic: String): ProducerRecord[String, String] =
    ProducerRecord(topic, key, body)

  def mkTopics(outputCount: Int = 2): Seq[String] =
    (1 to outputCount).map { n =>
      s"output$n"
    }

  def mkProducer(bootstrap: String): Resource[IO, KafkaProducer[IO, String, String]] = {
    val settings = ProducerSettings[IO, String, String]
      .withBootstrapServers(bootstrap)
      .withClientId("test-app")

    KafkaProducer.resource(settings)
  }

  def mkRecords(topicsCount: Int, recordsPerTopic: Int = 1): ProducerRecords[Unit, String, String] = ProducerRecords(
    (1 to topicsCount).flatMap { topicNr =>
      (1 to recordsPerTopic).map(n => mkRecord(s"$n", s"hello$n", s"output$topicNr"))
    }.toList
  )

  def push[P](records: ProducerRecords[P, String, String])(
    producer: KafkaProducer[IO, String, String]
  ): IO[ProducerResult[P, String, String]] =
    producer.produce(records).flatten
}
