package it.unibo.demo.scenarios

class FollowTheLeaderRotating extends BaseDemo:
  private val directions = List(
    (0.5, 0.5),
    (0.5, -0.5),
    (-0.5, -0.5),
    (-0.5, 0.5),
  )
  override def main(): (Double, Double) =
    val currentTime = System.currentTimeMillis()
    val timeSlot = 10000
    val result = rep((currentTime, 0, (0.0, 0.0))): (prevTime, prevIndex, _) =>
      val deltaTime = currentTime - prevTime
      val newTime = if deltaTime > timeSlot then currentTime else prevTime
      val newIndex = if deltaTime > timeSlot then prevIndex + 1 else prevIndex
      val newLeaderDirection = normalize(directions(newIndex % directions.length))
      val result = gradientCast[(Double, Double)](mid() == 6, newLeaderDirection, identity)
      if (mid() == 6) println(newLeaderDirection)
      (newTime, newIndex, result)
    result._3
