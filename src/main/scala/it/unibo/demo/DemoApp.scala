package it.unibo.demo

import it.unibo.core.UpdateLoop
import it.unibo.core.aggregate.AggregateIncarnation.ID
import it.unibo.mock.AggregateServiceExample.stage
import it.unibo.core.aggregate.AggregateIncarnation.*
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.mock.{MagnifierPolicy, SimpleRender}
import scalafx.application.JFXApp3
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import view.fx.{NeighborhoodPanel, NodeStyle, WorldPanel}
import it.unibo.utils.Position.{*, given}
import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.opencv_java

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
    (1, 0)

object AggregateServiceExample extends JFXApp3 {
  try Loader.load(classOf[opencv_java])
  private val agentsNeighborhoodRadius = 200
  private val nodeGuiSize = 5
  override def start(): Unit =

    val provider = CameraProvider(
      List(
        //0,
        // 1,
        2,
        3,
        // 4
    ), 1)
    val robots = List(
      //WaveRobot("192.168.8.10", 0),
      //WaveRobot("192.168.8.11", 1),
      WaveRobot("192.168.8.12", 2),
      WaveRobot("192.168.8.13", 3),
      //WaveRobot("192.168.8.14", 4)
    )
    val update = RobotUpdate(robots)
    val aggregateOrchestrator =
      AggregateOrchestrator[Position, Info, (Double, Double)](robots.map(_.id).toSet, new Program)
    val magnifier = MagnifierPolicy.translateAndScale((300, 200), 800)
    val basePane = Pane()
    val guiPane = Pane()
    val neighborhoodPane = Pane()
    basePane.children.addAll(neighborhoodPane, guiPane)
    val worldPane = WorldPanel(guiPane, NodeStyle(nodeGuiSize, nodeGuiSize * 2, Color.Blue))
    val neighbouringPane = NeighborhoodPanel(guiPane, neighborhoodPane)
    val render = SimpleRender(worldPane, neighbouringPane, magnifier)

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
