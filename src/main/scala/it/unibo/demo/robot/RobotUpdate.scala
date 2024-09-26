package it.unibo.demo.robot

import it.unibo.core.{Environment, EnvironmentUpdate}
import it.unibo.demo.robot.Actuation.{NoOp, Rotation, Forward}
import it.unibo.demo.{ID, Info, Position}
import it.unibo.demo.robot.Robot

import scala.concurrent.{ExecutionContext, Future}

enum Actuation:
  case Rotation(rotationVector: (Double, Double))
  case Forward(vector: (Double, Double))
  case NoOp

class RobotUpdate(robots: List[Robot], threshold: Double)(using ExecutionContext)
    extends EnvironmentUpdate[ID, Position, Actuation, Info, Environment[ID, Position, Info]]:

  override def update(world: Environment[ID, Position, Info], id: ID, actuation: Actuation): Future[Unit] =
    val robot = robots.find(_.id == id).get
    actuation match
      case _ if !world.nodes.contains(id) => Future(robot.nop())
      case NoOp => Future(robot.nop())
      case Rotation(actuation) =>
        Future:
          val direction = world.sensing(id)
          val directionVector = (Math.cos(direction), Math.sin(direction))
          val rotationVector = (actuation._2, actuation._1)
          val deltaAngle =
            math.atan2(rotationVector._2, rotationVector._1) - Math.atan2(directionVector._2, directionVector._1)
          val angleEuclideanDistance = Math.sqrt(
            (rotationVector._1 - directionVector._1) * (rotationVector._1 - directionVector._1) +
              (rotationVector._2 - directionVector._2) * (rotationVector._2 - directionVector._2)
          )
          angleEuclideanDistance match
            case _ if angleEuclideanDistance < threshold => robot.nop()
            case _ if deltaAngle > 0 => robot.spinRight()
            case _ => robot.spinLeft()
      case Forward(actuation) =>
        Future:
          val direction = world.sensing(id)
          val directionVector = (Math.cos(direction), Math.sin(direction))
          val adjustedVector = (actuation._2, actuation._1)
          val vectorDirection = Math.atan2(actuation._2, actuation._1)
          val vector = if (vectorDirection < 0) (-adjustedVector._1, -adjustedVector._2) else adjustedVector
          println("Vector: " + vector)
          println("Actuation: " + adjustedVector)
          println("Vector direction: " + vectorDirection)
          val deltaAngle = Math.atan2(vector._2, vector._1) - Math.atan2(directionVector._2, directionVector._1)
          val angleEuclideanDistance = Math.sqrt(
            (vector._1 - directionVector._1) * (vector._1 - directionVector._1) +
              (vector._2 - directionVector._2) * (vector._2 - directionVector._2)
          )
          angleEuclideanDistance match
            case _ if angleEuclideanDistance < threshold =>
              if (vectorDirection < 0) robot.backward() else robot.forward()
            case _ if deltaAngle > 0 => robot.spinRight()
            case _ => robot.spinLeft()

//    Future:
//      val direction = world.sensing(id)
//      val directionVector = (Math.cos(direction), Math.sin(direction))
//      val euclideanDistance = Math.sqrt(
//        (actuation._1 - directionVector._1) * (actuation._1 - directionVector._1) +
//          (actuation._2 - directionVector._2) * (actuation._2 - directionVector._2)
//      )
//      // get the angle between direction and actuation
//      val angle = Math.atan2(actuation._2, actuation._1) - Math.atan2(directionVector._2, directionVector._1)
//      val selected = robots.find(_.id == id)
//      robots
//        .find(_.id == id)
//        .foreach: robot =>
//          if actuation == (0.0, 0.0) then robot.nop()
//          else if euclideanDistance < threshold then robot.backward()
//          else if angle > 0 then robot.spinRight()
//          else robot.spinLeft()
//          end if
//      ()
