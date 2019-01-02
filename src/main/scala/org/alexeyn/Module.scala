package org.alexeyn

import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives.concat
import cats.instances.future.catsStdInstancesForFuture
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import org.alexeyn.http.{CommandRoutes, QueryRoutes}
import org.alexeyn.json.SprayJsonCodes._
import slick.jdbc.PostgresProfile.api._
import com.softwaremill.macwire._

import scala.concurrent.{ExecutionContext, Future}

class Module(cfg: Config, createSchema: Boolean = true)(implicit system: ActorSystem, ec: ExecutionContext)
    extends StrictLogging {

  val db = Database.forConfig("storage", cfg)
  val dao = wire[TripDao]
  val service = wire[TripService[Future]]
  val routes = concat(wire[QueryRoutes].routes, wire[CommandRoutes].routes)

  if (createSchema) _createSchema()

  private def _createSchema(): Unit =
    dao.createSchema().failed.foreach(t => logger.error(s"Failed to create schema: $t"))

  def close(): Unit = db.close()
}
