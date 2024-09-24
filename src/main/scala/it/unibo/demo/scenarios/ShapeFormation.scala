package it.unibo.demo.scenarios

abstract class ShapeFormation(leaderSelected: Int, stabilityThreshold: Double) extends BaseDemo:
  def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)]

  override def main(): (Double, Double) =
    val leader = mid() == leaderSelected
    val potential = gradientCast(leader, 0.0, _ + nbrRange)
    val directionTowardsLeader = gradientCast(leader, (0.0, 0.0), (x, y) => (x + distanceVector._1, y + distanceVector._2))
    val collectInfo = collectCast[Set[(Int, (Double, Double))]](potential, _ ++ _, Set((mid(), directionTowardsLeader)), Set.empty).filter(_._1 != mid())
    val ordered = orderedNodes(collectInfo)
    val suggestion = calculateSuggestion(ordered)
    val local = gradientCast(leader, suggestion, a => a).getOrElse(mid, (0.0, 0.0))
    val distanceTowardGoal = Math.sqrt(local._1 * local._1 + local._2 * local._2)
    if distanceTowardGoal < stabilityThreshold then (0, 0)
    else (local._1 / distanceTowardGoal, local._2 / distanceTowardGoal)

  protected def orderedNodes(nodes: Set[(Int, (Double, Double))]): List[(Int, (Double, Double))] =
    nodes.filter(_._1 != mid()).toList.sortBy(_._1)

class LineFormation(distanceThreshold: Double, leaderSelected: Int, stabilityThreshold: Double) extends ShapeFormation(leaderSelected, stabilityThreshold):
  override def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)] =
    val (left, right) = ordered.splitAt(ordered.size / 2)
    val leftSuggestion = left.zipWithIndex.map { case ((id, (x, y)), i) => id -> (((-(i + 1) * distanceThreshold) + x, y)) }.toMap
    val rightSuggestion = right.zipWithIndex.map { case ((id, (x, y)), i) => id -> ((((i + 1) * distanceThreshold) + x, y)) }.toMap
    leftSuggestion ++ rightSuggestion

class CircleFormation(radius: Double, leaderSelected: Int, stabilityThreshold: Double) extends ShapeFormation(leaderSelected, stabilityThreshold):
  override def calculateSuggestion(ordered: List[(Int, (Double, Double))]): Map[Int, (Double, Double)] =
    val division = (math.Pi * 2) / ordered.size
    ordered.zipWithIndex.map { case ((id, (x, y)), i) =>
      val angle = division * (i + 1)
      id -> ((math.sin(angle) * radius + x, math.cos(angle) * radius + y))
    }.toMap