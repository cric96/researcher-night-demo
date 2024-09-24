package it.unibo.demo.robot

import it.unibo.core.{Environment, EnvironmentUpdate}
import it.unibo.demo.robot.Robot
import it.unibo.demo.{ID, Info, Position}

import scala.concurrent.{ExecutionContext, Future}

class RobotUpdateWithoutRotate(robots: List[Robot], threshold: Double)(using ExecutionContext)
    extends EnvironmentUpdate[ID, Position, (Double, Double), Info, Environment[ID, Position, Info]]:
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
          else
            val maxV = 0.5
            val vRotation = (actuation._1 - actuation._2) / 1
            val vX = actuation._1 - 0.6 * vRotation
            val vY = actuation._2 * 0.6 * vRotation
            println(s"vX: $vX, vY: $vY")
            def map(minRef: Double, maxRef: Double, min: Double, max: Double, value: Double): Double =
              min + (max - min) * ((value - minRef) / (maxRef - minRef))
            val adaptedX = map(-1, 1, -maxV, maxV, vX)
            val adaptedY = map(-1, 1, -maxV, maxV, vY)
            println(s"adaptedX: $adaptedX, adaptedY: $adaptedY")
            robot.intensities(adaptedX, adaptedY)
          end if
      ()
