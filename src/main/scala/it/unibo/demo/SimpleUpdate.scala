package it.unibo.demo

import it.unibo.core.EnvironmentUpdate

import scala.concurrent.Future

class SimpleUpdate(world: SimpleEnvironment) extends EnvironmentUpdate[ID, (Double, Double)]:
  override def update(node: ID, actuation: (Double, Double)): Future[Info] =
    Future.successful:
      val position = world.positions(node)
      world.positions = world.positions.updated(node, (position._1 + actuation._1, position._2 + actuation._2))

