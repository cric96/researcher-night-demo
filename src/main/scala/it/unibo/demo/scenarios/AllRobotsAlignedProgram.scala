package it.unibo.demo.scenarios

import it.unibo.core.aggregate.AggregateIncarnation.{AggregateProgram, BlockG, StandardSensors}

class AllRobotsAlignedProgram extends AggregateProgram, StandardSensors, BlockG:
  private def distanceVector: (Double, Double) = nbrvar(NBR_VECTOR)
  private def module(position: (Double, Double)): Double =
    Math.sqrt(position._1 * position._1 + position._2 * position._2)

  private def normalize(position: (Double, Double)): (Double, Double) =
    val module = this.module(position)
    (position._1 / module, position._2 / module)

  override def main(): (Double, Double) = normalize((1.0, 0.0))
