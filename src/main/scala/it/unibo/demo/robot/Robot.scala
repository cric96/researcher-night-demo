package it.unibo.demo.robot

import java.net.URLEncoder

trait Robot:
  def id: Int
  def spinRight(): Unit
  def spinLeft(): Unit
  def forward(): Unit
  def backward(): Unit
  def nop(): Unit
  def intensities(left: Double, right: Double): Unit

class WaveRobot(ip: String, val id: Int) extends Robot:
  private var lastCommandWasNoOp = false
  def spinRight(): Unit =
    requests.get(url = s"http://$ip/js?json=${Command(0.22, -0.22).toJson}")
    lastCommandWasNoOp = false
  def spinLeft(): Unit =
    requests.get(url = s"http://$ip/js?json=${Command(-0.22, 0.22).toJson}")
    lastCommandWasNoOp = false
  def forward(): Unit =
    requests.get(url = s"http://$ip/js?json=${Command(-0.16, -0.16).toJson}")
    lastCommandWasNoOp = false
  def backward(): Unit =
    requests.get(url = s"http://$ip/js?json=${Command(0.16, 0.16).toJson}")
    lastCommandWasNoOp = false
  def intensities(left: Double, right: Double): Unit =
    requests.get(url = s"http://$ip/js?json=${Command(left, right).toJson}")
    lastCommandWasNoOp = false
  override def nop(): Unit =
    if !lastCommandWasNoOp then requests.get(url = s"http://$ip/js?json=${Command(0, 0).toJson}")
    else ()
    lastCommandWasNoOp = true
  private class Command(left: Double, right: Double):
    def toJson: String = URLEncoder.encode(s"""{"T":1, "L":$left, "R":$right}""")
