package it.unibo.demo.robot

import java.net.URLEncoder

trait Robot:
  def id: Int
  def spinRight(): Unit
  def spinLeft(): Unit
  def forward(): Unit
  def backward(): Unit
  def nop(): Unit

class WaveRobot(ip: String, val id: Int) extends Robot:
  def spinRight(): Unit = requests.get(url = s"http://$ip/js?json=${Command(0.25, -0.25).toJson}")
  def spinLeft(): Unit = requests.get(url = s"http://$ip/js?json=${Command(-0.25, 0.25).toJson}")
  def forward(): Unit = requests.get(url = s"http://$ip/js?json=${Command(0.25, 0.25).toJson}")
  def backward(): Unit = requests.get(url = s"http://$ip/js?json=${Command(-0.25, -0.25).toJson}")
  override def nop(): Unit = requests.get(url = s"http://$ip/js?json=${Command(-0, -0).toJson}")
  private class Command(left: Double, right: Double):
    def toJson: String = URLEncoder.encode(s"""{"T":1, "L":$left, "R":$right}""")
