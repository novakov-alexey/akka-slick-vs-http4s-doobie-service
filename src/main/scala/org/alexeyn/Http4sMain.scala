package org.alexeyn

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import com.typesafe.scalalogging.StrictLogging
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.implicits._
import scala.concurrent.ExecutionContext.Implicits.global

object Http4sMain extends IOApp with StrictLogging {
  val (server, jdbc, cfg) =
    AppConfig.load.fold(e => sys.error(s"Failed to load configuration:\n${e.toList.mkString("\n")}"), identity)

  val mod = new Http4sModule(jdbc)
  mod.init().unsafeToFuture().failed.foreach(t => logger.error("Failed to initialize Trips module", t))

  val apiV1App = mod.routes.orNotFound

  def run(args: List[String]): IO[ExitCode] =
    BlazeServerBuilder[IO]
      .bindHttp(server.port.value, server.host.value)
      .withHttpApp(apiV1App)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)
}