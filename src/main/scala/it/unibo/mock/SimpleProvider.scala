package it.unibo.mock

import it.unibo.core.Environment
import it.unibo.core.EnvironmentProvider
import scala.concurrent.Future

class SimpleProvider(world: SimpleEnvironment) extends EnvironmentProvider[ID, Position, Info, SimpleEnvironment]:
  def provide(): Future[SimpleEnvironment] = Future.successful(world)
