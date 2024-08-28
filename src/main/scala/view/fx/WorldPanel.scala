package view.fx

import scalafx.Includes.{jfxCircle2sfx, jfxNode2sfx}
import scalafx.application.Platform
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.Circle

/**
 * A panel that represents the world of the simulation.
 * It is a pane that contains all the nodes of the world.
 * @param pane The pane that contains the nodes.
 * @param radius The radius of the nodes.
 */
class WorldPanel(pane: Pane, style: NodeStyle):
  def drawNodeAt(id: String, x: Double, y: Double): Unit =
    Platform.runLater:
      if !isAlreadyIn(id) then
        val node = Circle(x, y, style.radius)
        node.id = id
        node.fill = style.color
        pane.children.addAll(node)
      else
        val node: Circle = pane.children.find(_.id.value == id)
          .get
          .asInstanceOf[javafx.scene.shape.Circle]
        node.centerX = x
        node.centerY = y

  private def isAlreadyIn(id: String): Boolean = pane.children.exists(_.id.value == id)

case class NodeStyle(radius: Double, color: Color)