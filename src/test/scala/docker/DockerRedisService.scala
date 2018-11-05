package docker

import cats.syntax.option._
import com.spotify.docker.client.DefaultDockerClient
import com.whisk.docker.impl.spotify.SpotifyDockerFactory
import com.whisk.docker.{DockerContainer, DockerFactory, DockerKit, DockerReadyChecker}

trait DockerRedisService extends DockerKit {

  override implicit val dockerFactory: DockerFactory = new SpotifyDockerFactory(DefaultDockerClient.fromEnv().build())

  val DefaultRedisPort = 6379

  val redisContainer = DockerContainer("redis:3.2.6-alpine")
    .withPorts((DefaultRedisPort, 6378.some))
    .withReadyChecker(DockerReadyChecker.LogLineContains("The server is now ready to accept connections on port 6379"))
    .withCommand("redis-server")

  abstract override def dockerContainers: List[DockerContainer] =
    redisContainer :: super.dockerContainers
}
