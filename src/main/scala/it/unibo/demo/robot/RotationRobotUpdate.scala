package it.unibo.demo.robot

import it.unibo.core.{Environment, EnvironmentUpdate}
import it.unibo.demo.{ID, Info, Position}

import scala.concurrent.{ExecutionContext, Future}

class RotationRobotUpdate(private val robots: List[Robot], private val threshold: Double)(using ExecutionContext)
    extends EnvironmentUpdate[ID, Position, (Double, Double), Info, Environment[ID, Position, Info]]:
  override def update(world: Environment[ID, Position, Info], id: ID, actuation: (Double, Double)): Future[Unit] =
    Future:
      val direction = world.sensing(id) // get the desired angle
      val directionVector = (Math.cos(direction), Math.sin(direction))
      val euclideanDistance = Math.sqrt(
        (actuation._1 - directionVector._1) * (actuation._1 - directionVector._1) +
          (actuation._2 - directionVector._2) * (actuation._2 - directionVector._2)
      )
      println(s"Node $id direction: $direction")
      println(s"Node $id actuation: $actuation")
      val angle = Math.atan2(actuation._2, actuation._1) - Math.atan2(directionVector._2, directionVector._1)
      println(s"Node $id angle: ${Math.atan2(actuation._2, actuation._1)}")
      robots.find(_.id == id) match
        case None => throw IllegalStateException(s"No robot with $id found!")
        case Some(robot) => handleMovement(robot, actuation, euclideanDistance, angle)

  private def handleMovement(robot: Robot, actuation: (Double, Double), distance: Double, angle: Double): Unit =
    actuation match
      case (0.0, 0.0) => robot.nop()
      case _ if distance < threshold => robot.nop()
      case _ if angle > 0 => robot.spinRight()
      case _ => robot.spinLeft()