package it.unibo.demo

import it.unibo.core.Environment
import it.unibo.utils.Position.Position
import it.unibo.core.DistanceEstimator.distance
import it.unibo.utils.Position.{*, given}

class DemoEnvironment(data: Map[ID, (Position, Info)], neighboursRadius: Double)
    extends Environment[ID, Position, Info]:

  override def nodes: Set[ID] = data.keySet

  override def position(id: ID): (Double, Double) = data(id)._1

  override def sensing(id: ID): Info = data(id)._2

  override def neighbors(id: ID): Set[ID] =
    data.filter { case (k, v) => data(id)._1.distance(v._1) <= neighboursRadius }.keys.toSet
