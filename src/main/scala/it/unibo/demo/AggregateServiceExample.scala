package it.unibo.demo

import it.unibo.core.UpdateLoop
import it.unibo.core.aggregate.AggregateIncarnation.*
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.demo.Position.given
import scalafx.application.JFXApp3
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import view.fx.{NeighborhoodPanel, NodeStyle, WorldPanel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

class Program extends AggregateProgram with StandardSensors with BlockG:
  def distanceVector: (Double, Double) = nbrvar[(Double, Double)](NBR_VECTOR)
  private val neighborsMinDistance = 10.0

  /**
   * A simple program in which the nodes go towards the center of a the node with the id 1.
   * @return the vector that the node has to follow to reach the center of the node with id 1.
   */
  override def main(): (Double, Double) =
    val leader = mid() == 1
    val neighbours = foldhood(0)(_ + _)(1)
    val distance =
      gradientCast[(Double, Double)](leader, (0.0, 0.0), (x, y) => (x + distanceVector._1, y + distanceVector._2))
    val module = Math.sqrt(distance._1 * distance._1 + distance._2 * distance._2)
    val towardCenters = (distance._1 / module, distance._2 / module)
    val minDistance = foldhoodPlus(Double.MaxValue)(Math.min)(nbrRange())
    if module == 0 || minDistance < neighborsMinDistance then (Random.nextGaussian() / 5.0, Random.nextGaussian() / 5.0)
    else towardCenters

object AggregateServiceExample extends JFXApp3 {
  private val agentsNeighborhoodRadius = 200
  private val nodeGuiSize = 5
  override def start(): Unit =

    val agents = randomAgents(40, 800)
    val world = SimpleEnvironment(agents, agentsNeighborhoodRadius)
    val provider = SimpleProvider(world)
    val update = SimpleUpdate(world)
    val aggregateOrchestrator = AggregateOrchestrator[Position, Info, (Double, Double)](agents.keySet, new Program)

    val basePane = Pane()
    val guiPane = Pane()
    val neighborhoodPane = Pane()
    basePane.children.addAll(neighborhoodPane, guiPane)
    val worldPane = WorldPanel(guiPane, NodeStyle(nodeGuiSize, Color.Blue))
    val neighbouringPane = NeighborhoodPanel(guiPane, neighborhoodPane)
    val render = SimpleRender(worldPane, neighbouringPane)

    UpdateLoop.loop(33)(
      provider,
      aggregateOrchestrator,
      update,
      render
    )

    stage = new JFXApp3.PrimaryStage:
      title = "Aggregate Service Example"
      scene = new scalafx.scene.Scene:
        content = basePane
      width = 800
      height = 800

  private def randomAgents(howMany: Int, maxPosition: Int): Map[ID, (Double, Double)] =
    val random = new scala.util.Random
    (1 to howMany).map { i =>
      i -> (random.nextDouble() * maxPosition, random.nextDouble() * maxPosition)
    }.toMap
}
