package it.unibo.mock

import it.unibo.core.EnvironmentUpdate

import scala.concurrent.Future

class SimpleUpdate extends EnvironmentUpdate[ID, Position, (Double, Double), Info, SimpleEnvironment]:
  override def update(world: SimpleEnvironment, id: ID, actuation: (Double, Double)): Future[Unit] = Future.successful:
    val position = world.positions(id)
    world.positions = world.positions.updated(id, (position._1 + actuation._1, position._2 + actuation._2))

