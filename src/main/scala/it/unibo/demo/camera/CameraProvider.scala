package it.unibo.demo.camera

import it.unibo.artificial_vision_tracking.aruco_markers.CameraPose
import it.unibo.core.{Environment, EnvironmentProvider}
import it.unibo.demo.environment.DemoEnvironment
import it.unibo.demo.{ID, Info, Position}
import org.opencv.core.Mat
import org.opencv.objdetect.Objdetect

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters.ListHasAsScala

class CameraProvider(val ids: List[ID], radius: Double)(using ExecutionContext)
    extends EnvironmentProvider[ID, Position, Info, Environment[ID, Position, Info]]:
  private var worldCache = ids.map(id => id -> ((0.0, 0.0) -> 0.0)).toMap
  private val concurrentQueue = new LinkedBlockingQueue[Map[ID, (Position, Info)]]()
//  private val markersX = 6
//  private val markersY = 6
  private val markerLength = 0.08f
  private val selectedCamera = 4
  private val dictionaryType = Objdetect.DICT_4X4_1000
  private val cameraParam = new java.util.ArrayList[Mat]
  private var cameraMatrix = Mat()
  private var distCoeffs = Mat()
  cameraMatrix = Mat(3, 3, org.opencv.core.CvType.CV_64F)
  private val data: Array[Double] =
    Array[Double](1340.821804232236, 0, 945.5377637384079, 0, 1339.251046705548, 581.4177912549047, 0, 0, 1)
//    Array[Double](1074.4836411064782, 0, 934.142462616438, 0, 1069.2850608442209, 492.71509255433847, 0, 0, 1)
  cameraMatrix.put(0, 0, data: _*)
  distCoeffs = new Mat(1, 5, org.opencv.core.CvType.CV_64F)
  private val data2: Array[Double] = Array[Double](-0.3898373600798533, 0.08115247413122996, -1.965974706520358e-05,
    -0.0006330161088470909, 0.1140937797457088)
//  private val data2: Array[Double] = Array[Double](0.1641931951105014, -0.7937802569526623, -0.01970636034087885,
//    0.002095446012401154, 0.9932870879349068)
  distCoeffs.put(0, 0, data: _*)
  cameraParam.add(cameraMatrix)
  cameraParam.add(distCoeffs)
  private val cameraPose =
    new CameraPose(cameraParam.get(0), cameraParam.get(1), markerLength, dictionaryType, selectedCamera)
  private val camera = cameraPose.getCamera
  Future:
    while true do createWorld()
  def provide(): Future[Environment[ID, Position, Info]] = Future:
    val data = concurrentQueue.take()
    concurrentQueue.clear()
    DemoEnvironment(data, radius)

  private def createWorld(): DemoEnvironment =
    val data = cameraPose
      .capturePositioning(camera)
      .asScala
      .map(p => (p.getId, (p.getX, p.getY) -> p.getRotation))
      .toMap
    // use old value if not present
    val updatedWorld = worldCache.map { case (id, (pos, rot)) => id -> data.getOrElse(id, (pos, rot)) }
    // adjust new value weighting the old one with the new one (both for position and rotation
    worldCache = updatedWorld.map { case (id, (pos, rot)) =>
      id -> (adaptPosition(worldCache(id)._1, pos), rot * 0.9 + worldCache(id)._2 * 0.1)
    }
    concurrentQueue.add(updatedWorld)
    DemoEnvironment(worldCache, radius)
    // DemoEnvironment(data, radius)

  private def adaptPosition(old: Position, newPos: Position): Position =
    val (x, y) = old
    val (newX, newY) = newPos
    (x * 0.1 + newX * 0.9, y * 0.1 + newY * 0.9)
