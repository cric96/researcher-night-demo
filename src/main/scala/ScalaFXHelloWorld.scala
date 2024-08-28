import scalafx.application.JFXApp3
import scalafx.scene.layout.Pane
import scalafx.scene.paint.Color
import view.fx.{NodeStyle, WorldPanel}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
class LeftWalkingNode(val name: String, var x: Int, var y: Int):
  def walk(): Unit =
    println(s"Node $name is walking")
    x += 10

def logic(pane: WorldPanel, node: LeftWalkingNode): Future[Unit] =
  for
    _ <- Future(node.walk())
    _ <- Future(pane.drawNodeAt(node.name, node.x, node.y))
    _ <- Future(Thread.sleep(100))
    _ <- logic(pane, node)
  yield ()

object ScalaFXHelloWorld extends JFXApp3 {
  override def start(): Unit = {
    val pane = Pane()
    val worldPane = WorldPanel(pane, NodeStyle(10, Color.Blue))
    logic(worldPane, LeftWalkingNode("A", 10, 10))
    stage = new JFXApp3.PrimaryStage:
      title = "Hello World"
      scene = new scalafx.scene.Scene:
        content = pane
      width = 800
      height = 600
  }
}
