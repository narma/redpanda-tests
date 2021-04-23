# Redpanda compatibility tests

Tests to find and fix compatibility issues with original Kafka.

## Usage

This projects uses [sbt](https://www.scala-sbt.org/) and [scalatest](https://www.scalatest.org/) with [testcontainers](https://www.testcontainers.org/) for run test suites.

### sbt tasks  
- Run all tests both for Redpanda and Kafka: `test`
- Run all tests for Redpanda: `testOnly app.RedpandaSpec`
- Run all tests for Kafka: `testOnly app.KafkaSpec`

## Structure
- All tests contains in `src/test/scala/app/AppTests.scala`
    
