package it.unibo.demo.scenarios

import it.unibo.core.aggregate.AggregateIncarnation.{AggregateProgram, BlockG, BlockC, StandardSensors}

trait BaseDemo extends AggregateProgram, StandardSensors, BlockG, BlockC:
  def distanceVector: (Double, Double) = nbrvar(NBR_VECTOR)

  def module(position: (Double, Double)): Double =
    Math.sqrt(position._1 * position._1 + position._2 * position._2)

  def normalize(position: (Double, Double)): (Double, Double) =
    val module = this.module(position)
    (position._1 / module, position._2 / module)
