package it.unibo.demo

import it.unibo.core.{Environment, EnvironmentUpdate}

import scala.concurrent.Future
class RobotUpdate(robots: List[Robot])
    extends EnvironmentUpdate[ID, Position, (Double, Double), Info, Environment[ID, Position, Info]]:
  val threshold = 0.15
  override def update(world: Environment[ID, Position, Info], id: ID, actuation: (Double, Double)): Future[Unit] =
    Future.successful[Unit]:
      val position = world.position(id)
      val direction = world.sensing(id)
      val directionVector = (Math.cos(direction), Math.sin(direction))
      val euclideanDistance = Math.sqrt(
        (actuation._1 - directionVector._1) * (actuation._1 - directionVector._1) +
          (actuation._2 - directionVector._2) * (actuation._2 - directionVector._2)
      )
      println(euclideanDistance)
      val selected = robots.find(_.id == id)
      robots
        .find(_.id == id)
        .foreach: robot =>
          if euclideanDistance < threshold then robot.forward()
          else robot.spin()
          end if
