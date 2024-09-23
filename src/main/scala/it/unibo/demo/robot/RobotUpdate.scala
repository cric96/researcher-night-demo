package it.unibo.demo

import it.unibo.core.{Environment, EnvironmentUpdate}
import it.unibo.demo.robot.Robot

import scala.concurrent.{ExecutionContext, Future}
class RobotUpdate(robots: List[Robot])(using ExecutionContext)
    extends EnvironmentUpdate[ID, Position, (Double, Double), Info, Environment[ID, Position, Info]]:
  val threshold = 0.15
  override def update(world: Environment[ID, Position, Info], id: ID, actuation: (Double, Double)): Future[Unit] =
    Future:
      val direction = world.sensing(id)
      val directionVector = (Math.cos(direction), Math.sin(direction))
      val euclideanDistance = Math.sqrt(
        (actuation._1 - directionVector._1) * (actuation._1 - directionVector._1) +
          (actuation._2 - directionVector._2) * (actuation._2 - directionVector._2)
      )
      // get the angle between direction and actuation
      val angle = Math.atan2(actuation._2, actuation._1) - Math.atan2(directionVector._2, directionVector._1)
      val selected = robots.find(_.id == id)
      robots
        .find(_.id == id)
        .foreach: robot =>
          if actuation == (0.0, 0.0) then robot.nop()
          else if euclideanDistance < threshold then robot.backward()
          else if angle > 0 then robot.spinRight()
          else robot.spinLeft()
          end if
      ()
