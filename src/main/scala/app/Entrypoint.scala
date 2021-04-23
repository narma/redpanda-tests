package app

import cats.effect.{ExitCode, IO, IOApp}
import wvlet.log.{LogFormatter, LogLevel, LogSupport, Logger}

object Entrypoint extends IOApp with LogSupport {
  def p(s: String): IO[Unit] = IO(info(s))

  def configureLogging(): Unit = {
    Logger.setDefaultLogLevel(LogLevel.INFO)
    Logger.scanLogLevels // scan log.properties
    Logger.setDefaultFormatter(LogFormatter.SourceCodeLogFormatter)
  }

  override def run(args: List[String]): IO[ExitCode] = for {
    _              <- IO(configureLogging())
    kafkaBootstrap <- IO(sys.env.getOrElse("KAFKA_BOOTSTRAP", "localhost:9092"))
    _              <- p(s"kafka bootstrap: $kafkaBootstrap")
  } yield ExitCode.Success
}
