package testcontainers

import com.dimafeng.testcontainers.SingleContainer
import com.github.dockerjava.api.command.InspectContainerResponse
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.containers.{GenericContainer => JavaGenericContainer}
import org.testcontainers.images.builder.Transferable
import org.testcontainers.utility.DockerImageName

import java.nio.charset.StandardCharsets

object RedpandaContainer {
  private val starterScript = "/testcontainers_start.sh";
  val dockerTag: String     = "v21.4.14"

  // testcontainers-scala ForEachTestContainer / ForAllTestContainers required com.dimafeng.testcontainers.Container trait
  class RedpandaScalaContainer(underlying: RedpandaContainer) extends SingleContainer[RedpandaContainer] {
    override implicit val container: RedpandaContainer = underlying
    def bootstrapServers: String                       = s"$host:${mappedPort(9092)}"
  }

  def apply(tag: String = dockerTag): RedpandaScalaContainer = new RedpandaScalaContainer(new RedpandaContainer(tag))
}

class RedpandaContainer(
  val tag: String = RedpandaContainer.dockerTag
) extends JavaGenericContainer[RedpandaContainer](DockerImageName.parse(s"vectorized/redpanda:$tag")) {

  import RedpandaContainer.starterScript

  withExposedPorts(9092)
  withCreateContainerCmdModifier(cmd => cmd.withEntrypoint("sh"))
  withCommand("-c", "while [ ! -f " + starterScript + " ]; do sleep 0.1; done; " + starterScript)
  // forLog works faster than forListeningPort but sometimes didn't work, hmm.
  // waitingFor(Wait.forLogMessage(".*Started Kafka API server.*", 1))
  waitingFor(Wait.forListeningPort())

  def bootstrapServers: String = s"$getHost:${getMappedPort(9092)}"

  override protected def containerIsStarting(containerInfo: InspectContainerResponse, reused: Boolean): Unit = {
    super.containerIsStarting(containerInfo)

    var command = "#!/bin/bash\n"
    command += "/usr/bin/rpk redpanda start --check=false --node-id 0 --overprovisioned "
    command += "--kafka-addr PLAINTEXT://0.0.0.0:29092,OUTSIDE://0.0.0.0:9092 "
    command += s"--advertise-kafka-addr PLAINTEXT://kafka:29092,OUTSIDE://$bootstrapServers"

    copyFileToContainer(
      Transferable.of(command.getBytes(StandardCharsets.UTF_8), 0x1ff),
      starterScript
    )
  }
}
