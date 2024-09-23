package it.unibo.demo.robot

import it.unibo.demo.robot.WaveRobot

object RobotsMovementSimple extends App:
  private def robotIps(id: Int): String = s"192.168.8.$id"
  val robots = List(
    10,
    //11,
    12,
    13,
    //14
  ).map(i => WaveRobot(robotIps(i), i))
  while true do
    robots.foreach(_.spinRight())
  end while
