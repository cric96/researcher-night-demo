package it.unibo.mock

import it.unibo.core.{Environment, Boundary}
import view.fx.{NeighborhoodPanel, WorldPanel}

import scala.concurrent.{ExecutionContext, Future}

class SimpleRender(
    worldPanel: WorldPanel,
    neighPanel: NeighborhoodPanel,
    magnifier: MagnifierPolicy = MagnifierPolicy.identity
)(using ExecutionContext)
    extends Boundary[Int, (Double, Double), Double]:

  override def output(environment: Environment[ID, (Double, Double), Info]): Future[Unit] =
    Future:
      environment.nodes
        .map(id => id -> (environment.position(id) -> environment.sensing(id)))
        .map { case (id, (position, info)) => id -> (magnifier.magnify(position), info) }
        .foreach { case (id, (position, info)) => worldPanel.drawNodeAt(id.toString, position._1, position._2, info) }
      neighPanel.clean()
      environment.nodes
        .flatMap(id => environment.neighbors(id).map(id -> _))
        .foreach(link => neighPanel.drawNeighbours(link._1.toString, link._2.toString))

trait MagnifierPolicy:
  def magnify(position: (Double, Double)): (Double, Double)

object MagnifierPolicy:
  def scale(scale: Double): MagnifierPolicy =
    (position: (Double, Double)) => (position._1 * scale, position._2 * scale)
  def translateAndScale(translation: (Double, Double), scale: Double): MagnifierPolicy =
    (position: (Double, Double)) => (position._1 * scale + translation._1, position._2 * scale + translation._2)
  def identity: MagnifierPolicy =
    (position: (Double, Double)) => position
