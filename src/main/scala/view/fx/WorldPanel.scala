package view.fx

import scalafx.Includes._
import scalafx.application.Platform
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import scalafx.scene.shape.{Rectangle, Line}

class WorldPanel(pane: Pane, style: NodeStyle):
  def drawNodeAt(id: String, x: Double, y: Double, direction: Double): Unit =
    Platform.runLater:
      if !isAlreadyIn(id) then
        addNode(id, x, y, direction)
      else
        updateNode(id, x, y, direction)

  private def addNode(id: String, x: Double, y: Double, direction: Double): Unit =
    val node = Rectangle(x, y, style.width, style.height)
    node.id = id
    node.fill = style.color
    val orientationLine = createOrientationLine(id, x, y, direction)
    pane.children.addAll(node, orientationLine)

  private def updateNode(id: String, x: Double, y: Double, direction: Double): Unit =
    val node: Rectangle = findNodeById[javafx.scene.shape.Rectangle](id)
    node.x = x
    node.y = y
    val orientationLine: Line = findNodeById[javafx.scene.shape.Line](s"${id}_orientation")
    updateOrientationLine(orientationLine, x, y, direction)

  private def createOrientationLine(id: String, x: Double, y: Double, direction: Double): Line =
    val (centerX, centerY) = (x + style.width / 2, y + style.height / 2)
    val (deltaX, deltaY) = calculateDelta(centerX, centerY, direction)
    val line = Line(centerX, centerY, deltaX, deltaY)
    line.stroke = Color.Black
    line.strokeWidth = 3
    line.id = s"${id}_orientation"
    line

  private def updateOrientationLine(line: Line, x: Double, y: Double, direction: Double): Unit =
    val (centerX, centerY) = (x + style.width / 2, y + style.height / 2)
    val (deltaX, deltaY) = calculateDelta(centerX, centerY, direction)
    line.startX = centerX
    line.startY = centerY
    line.endX = deltaX
    line.endY = deltaY

  private def calculateDelta(centerX: Double, centerY: Double, direction: Double): (Double, Double) =
    val orientation = direction * 180 / Math.PI
    val deltaX = centerX + (Math.cos(orientation) * 10)
    val deltaY = centerY + (Math.sin(orientation) * 10)
    (deltaX, deltaY)

  private def findNodeById[T](id: String): T =
    pane.children.find(_.id.value == id).get.asInstanceOf[T]

  private def isAlreadyIn(id: String): Boolean =
    pane.children.exists(_.id.value == id)

case class NodeStyle(width: Double, height: Double, color: Color)