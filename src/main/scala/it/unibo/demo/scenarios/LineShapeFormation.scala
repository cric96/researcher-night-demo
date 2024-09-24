package it.unibo.demo.scenarios

class LineShapeFormation extends BaseDemo:

  override def main(): (Double, Double) =
    val currentTime = System.currentTimeMillis()
    val leader = 0
    val potential = gradientCast(leader == mid, 0.0, _ + nbrRange())
    ???
