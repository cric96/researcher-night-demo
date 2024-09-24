package it.unibo.demo.scenarios

class TowardLeader(leader: Int) extends BaseDemo:
  override def main(): (Double, Double) =
    val distance =
      gradientCast[(Double, Double)](
        leader == mid(),
        (0.0, 0.0),
        (x, y) => (x + distanceVector._1, y + distanceVector._2)
      )
    val module = Math.sqrt(distance._1 * distance._1 + distance._2 * distance._2)
    rotate90(mux(leader == mid())((0.0, 0.0))(normalize(distance)))
