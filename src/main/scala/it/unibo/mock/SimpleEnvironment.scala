package it.unibo.mock

import it.unibo.core.Environment
import it.unibo.core.DistanceEstimator.*
import it.unibo.utils.Position.{*, given}

class SimpleEnvironment(var positions: Map[ID, Position], neighboursRadius: Double)
    extends Environment[ID, Position, Info]:

  override def nodes: Set[ID] = positions.keySet

  override def position(id: ID): (Double, Double) = positions(id)

  override def sensing(id: ID): Info = 0

  override def neighbors(id: ID): Set[ID] =
    positions.filter { case (k, v) => positions(id).distance(v) <= neighboursRadius }.keys.toSet
