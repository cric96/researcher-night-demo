package it.unibo.mock

import it.unibo.core.UpdateLoop
import it.unibo.core.aggregate.AggregateIncarnation.*
import it.unibo.core.aggregate.AggregateOrchestrator
import it.unibo.demo.scenarios.{CircleFormation, LineFormation, TowardLeader}
import it.unibo.utils.Position.given
import scalafx.application.JFXApp3
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import view.fx.{NeighborhoodPanel, NodeStyle, WorldPanel}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Random

object AggregateServiceExample extends JFXApp3 {
  private val agentsNeighborhoodRadius = 1000
  private val nodeGuiSize = 5
  override def start(): Unit =

    val agents = randomAgents(20, 400)
    val world = SimpleEnvironment(agents, agentsNeighborhoodRadius)
    val provider = SimpleProvider(world)
    val update = SimpleUpdate()
    val aggregateOrchestrator =
      AggregateOrchestrator[Position, Info, Actuation](agents.keySet, new TowardLeader(10))

    val basePane = Pane()
    val guiPane = Pane()
    val neighborhoodPane = Pane()
    basePane.children.addAll(neighborhoodPane, guiPane)
    val worldPane = WorldPanel(guiPane, NodeStyle(nodeGuiSize, nodeGuiSize, Color.Blue))
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
