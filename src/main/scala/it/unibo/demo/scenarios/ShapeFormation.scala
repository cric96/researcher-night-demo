package it.unibo.demo.scenarios

abstract class ShapeFormation(leaderSelected: Int, stabilityThreshold: Double) extends BaseDemo:
  def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)]

  override def main(): (Double, Double) =
    val leader = mid() == leaderSelected
    val potential = gradientCast(leader, 0.0, _ + nbrRange)
    val directionTowardsLeader =
      gradientCast(leader, (0.0, 0.0), (x, y) => (x + distanceVector._1, y + distanceVector._2))
    val collectInfo =
      collectCast[Map[Int,(Double, Double)]](potential, _ ++ _, Map((mid() -> directionTowardsLeader)), Map.empty)
        .filter(_._1 != mid())
    val ordered = orderedNodes(collectInfo.toSet)
    val suggestion = branch(leaderSelected == mid())(calculateSuggestion(ordered))(Map.empty)
    val local = gradientCast(leader, suggestion, a => a).getOrElse(mid, (0.0, 0.0))
    val distanceTowardGoal = Math.sqrt(local._1 * local._1 + local._2 * local._2)
    if distanceTowardGoal < stabilityThreshold then (0, 0)
    else rotate90(local._1 / distanceTowardGoal, local._2 / distanceTowardGoal)

  protected def orderedNodes(nodes: Set[(Int, (Double, Double))]): List[(Int, (Double, Double))] =
    nodes.filter(_._1 != mid()).toList.sortBy(_._1)

class LineFormation(distanceThreshold: Double, leaderSelected: Int, stabilityThreshold: Double)
    extends ShapeFormation(leaderSelected, stabilityThreshold):
  override def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)] =
    //val (left, right) = ordered.splitAt(ordered.size / 2)
    val (leftSlots, rightSlots) = ordered.indices.splitAt(ordered.size / 2)
    var devicesAvailable = ordered
    val leftCandidates = leftSlots.map: index =>
      val candidate = devicesAvailable.map:
        case (id, (xPos, yPos)) =>
          val newPos @ (newXpos, newYpos) = ((-(index + 1) * distanceThreshold) + xPos, yPos)
          val modulo = math.sqrt((newXpos * newXpos) + (newYpos * newYpos))
          (id, modulo, newPos)
      .minBy(_._2)
      devicesAvailable = devicesAvailable.filterNot(_._1 == candidate._1)
      candidate._1 -> candidate._3
    .toMap
    println(leftCandidates)
    val rightCandidates = rightSlots.map(i => i - rightSlots.min).map: index =>
      val candidate = devicesAvailable.map:
        case (id, (xPos, yPos)) =>
          val newPos @ (newXpos, newYpos) = (((index + 1) * distanceThreshold) + xPos, yPos)
          val modulo = math.sqrt((newXpos * newXpos) + (newYpos * newYpos))
          (id, modulo, newPos)
      .minBy(_._2)
      devicesAvailable = devicesAvailable.filterNot(_._1 == candidate._1)
      candidate._1 -> candidate._3
    .toMap
    println(rightCandidates)
//    val leftSuggestion = left.zipWithIndex.map { case ((id, (x, y)), i) =>
//      id -> ((-(i + 1) * distanceThreshold) + x, y)
//    }.toMap
//    val rightSuggestion = right.zipWithIndex.map { case ((id, (x, y)), i) =>
//      id -> (((i + 1) * distanceThreshold) + x, y)
//    }.toMap
    leftCandidates ++ rightCandidates

class CircleFormation(radius: Double, leaderSelected: Int, stabilityThreshold: Double)
    extends ShapeFormation(leaderSelected, stabilityThreshold):
  override def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)] =
    val division = (math.Pi * 2) / ordered.size
    val precomputedAngels = ordered.indices.map(i => division * (i + 1))
    var availableDevices = ordered
    precomputedAngels
      .map: angle =>
        val candidate = availableDevices
          .map:
            case (id, (xPos, yPos)) =>
              val newPos @ (newXpos, newYpos) = (math.sin(angle) * radius + xPos, math.cos(angle) * radius + yPos)
              (id, math.sqrt((newXpos * newXpos) + (newYpos * newYpos)), newPos)
          .minBy(_._2)
        availableDevices = removeDeviceFromId(candidate._1, availableDevices)
        candidate._1 -> candidate._3
      .toMap

  private def removeDeviceFromId(id: Int, devices: List[(Int, (Double, Double))]): List[(Int, (Double, Double))] =
    devices.filterNot: (currentId, _) =>
      currentId == id

  private def nearestFromPoint(point: (Double, Double), devices: List[(Int, (Double, Double))]): Int =
    devices
      .map:
        case (id, (xPos, yPos)) =>
          val xDelta = math.abs(point._1 - xPos)
          val yDelta = math.abs(point._2 - yPos)
          id -> math.sqrt((xDelta * xDelta) + (yDelta * yDelta))
      .minBy(_._1)
      ._1
