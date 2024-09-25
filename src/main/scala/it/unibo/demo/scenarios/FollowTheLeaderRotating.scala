package it.unibo.demo.scenarios

class FollowTheLeaderRotating(private val leaderId: Int, private val timeInterval: Long = 10_000L) extends BaseDemo:
  private val directions = List(
    (0.5, 0.5),
    (0.5, -0.5),
    (-0.5, -0.5),
    (-0.5, 0.5)
  )

  override def main(): (Double, Double) =
    val currentTime = System.currentTimeMillis()
    val result = rep((currentTime, 0, (0.0, 0.0))): (prevTime, prevIndex, _) =>
      val deltaTime = currentTime - prevTime
      val newTime = if deltaTime > timeInterval then currentTime else prevTime
      val newIndex = if deltaTime > timeInterval then prevIndex + 1 else prevIndex
      val newLeaderDirection = normalize(directions(newIndex % directions.length))
      val result = gradientCast[(Double, Double)](mid() == 6, newLeaderDirection, identity)
      if (mid() == leaderId) println(newLeaderDirection)
      (newTime, newIndex, result)
    result._3
