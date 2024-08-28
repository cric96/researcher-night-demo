package it.unibo.demo

import it.unibo.core.Environment
import it.unibo.core.EnvironmentProvider
import scala.concurrent.Future

class SimpleProvider(world: Environment[ID, Position, Info]) extends EnvironmentProvider[ID, Position, Info]:
  def provide(): Future[Environment[ID, Position, Info]] = Future.successful(world)
