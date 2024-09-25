package it.unibo.demo.scenarios

import it.unibo.demo.robot.Actuation
import it.unibo.demo.robot.Actuation.*

class FollowTheLeaderRotating(private val leaderId: Int, private val timeInterval: Long = 10_000L) extends BaseDemo:
  private val directions = List(
    Actuation.Rotation(normalize(0.5, 0.5)),
    Actuation.Rotation(normalize(0.5, -0.5)),
    Actuation.Rotation(normalize(-0.5, -0.5)),
    Actuation.Rotation(normalize(-0.5, 0.5)),
  )

  override def main(): Actuation =
    val currentTime = System.currentTimeMillis()
    val result = rep((currentTime, 0, NoOp)): (prevTime, prevIndex, _) =>
      val deltaTime = currentTime - prevTime
      val newTime = if deltaTime > timeInterval then currentTime else prevTime
      val newIndex = if deltaTime > timeInterval then prevIndex + 1 else prevIndex
      val newLeaderDirection = directions(newIndex % directions.length)
      val result = gradientCast[Actuation](mid() == 6, newLeaderDirection, identity)
      if (mid() == leaderId) println(newLeaderDirection)
      (newTime, newIndex, result)
    result._3
