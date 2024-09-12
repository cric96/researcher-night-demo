package it.unibo.demo

import it.unibo.artificial_vision_tracking.aruco_markers.CameraPose
import it.unibo.core.{Environment, EnvironmentProvider}
import org.opencv.core.Mat
import org.opencv.objdetect.Objdetect

import scala.concurrent.Future
import scala.jdk.CollectionConverters.ListHasAsScala
class CameraProvider(val ids: List[ID], radius: Double)
    extends EnvironmentProvider[ID, Position, Info, Environment[ID, Position, Info]]:

  private var worldCache = ids.map(id => id -> ((0.0, 0.0) -> 0.0)).toMap
  private val markersX = 11
  private val markersY = 8
  private val markerLength = 0.07f
  private val selectedCamera = 0
  private val dictionaryType = Objdetect.DICT_4X4_100
  private val cameraParam = new java.util.ArrayList[Mat]
  private var cameraMatrix = Mat()
  private var distCoeffs = Mat()
  cameraMatrix = Mat(3, 3, org.opencv.core.CvType.CV_64F)
  private val data: Array[Double] =
    Array[Double](1340.821804232236, 0.0, 945.5377637384079, 0.0, 1339.251046705548, 581.4177912549047, 0.0, 0.0, 1.0)
  cameraMatrix.put(0, 0, data: _*)
  distCoeffs = new Mat(1, 5, org.opencv.core.CvType.CV_64F)
  private val data2: Array[Double] = Array[Double](-0.3898373600798533, 0.08115247413122996, -1.965974706520358e-05,
    -0.0006330161088470909, 0.1140937797457088)
  distCoeffs.put(0, 0, data: _*)
  cameraParam.add(cameraMatrix)
  cameraParam.add(distCoeffs)
  private val cameraPose =
    new CameraPose(cameraParam.get(0), cameraParam.get(1), markerLength, dictionaryType, selectedCamera)
  private val camera = cameraPose.getCamera
  def provide(): Future[Environment[ID, Position, Info]] = Future.successful(createWorld())

  def createWorld(): DemoEnvironment = {
    val data = cameraPose
      .capturePositioning(camera)
      .asScala
      .map(p => (p.getId, (p.getX, p.getY) -> p.getRotation))
      .toMap
    // use old value if not present
    worldCache = worldCache.map { case (id, (pos, rot)) => id -> data.getOrElse(id, (pos, rot)) }
    DemoEnvironment(worldCache, radius)
    // DemoEnvironment(data, radius)
  }
