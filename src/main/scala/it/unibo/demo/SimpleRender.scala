package it.unibo.demo

import it.unibo.core.{Environment, Boundary}
import view.fx.{NeighborhoodPanel, WorldPanel}

import scala.concurrent.{ExecutionContext, Future}

class SimpleRender(worldPanel: WorldPanel, neighPanel: NeighborhoodPanel)(using ExecutionContext) extends Boundary[ID, Position, Info]:

  override def output(environment: Environment[ID, (Double, Double), Info]): Future[Info] =
    Future:
      environment
        .nodes
        .map(id => id -> environment.position(id))
        .foreach((id, position) => worldPanel.drawNodeAt(id.toString, position._1, position._2))
      neighPanel.clean()
      environment
        .nodes
        .flatMap(id => environment.neighbors(id).map(id -> _))
        .foreach(link => neighPanel.drawNeighbours(link._1.toString, link._2.toString))