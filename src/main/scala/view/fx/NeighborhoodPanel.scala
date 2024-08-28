package view.fx

import scalafx.Includes.{jfxCircle2sfx, jfxNode2sfx}
import scalafx.application.Platform
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Circle, Line}

/**
 * This class is responsible for drawing the neighborhood of a node.
 * It requires the nodePane and the neighborhoodPanel to draw the lines.
 * The nodePane is used to get the position of the nodes.
 * The neighborhoodPanel is used to draw the lines.
 * @param nodePane: Pane where the nodes are drawn
 * @param neighborhoodPanel: Pane where the neighborhood is drawn
 */
class NeighborhoodPanel(nodePane: Pane, neighborhoodPanel: Pane):
  /**
   * This method should be called before drawing the neighborhood (each time!)
   * It removes all the lines from the neighborhoodPanel.
   */
  def clean(): Unit =
    Platform.runLater:
      neighborhoodPanel.children.clear()

  /**
   * This method draws a node in the nodePane.
   * @param id the id of the starting node
   * @param other the id of the node to draw
   */
  def drawNeighbours(id: String, other: String): Unit =
    Platform.runLater:
      if nodePane.isAlreadyIn(id) && nodePane.isAlreadyIn(other) then
        val circle1 = nodePane.children.find(_.id.value == id).get.asInstanceOf[javafx.scene.shape.Circle]
        val circle2 = nodePane.children.find(_.id.value == other).get.asInstanceOf[javafx.scene.shape.Circle]
        val line = Line(circle1.centerX.value, circle1.centerY.value, circle2.centerX.value, circle2.centerY.value)
        line.stroke = Color(0, 0, 0, 0.05)
        neighborhoodPanel.children.add(line)

