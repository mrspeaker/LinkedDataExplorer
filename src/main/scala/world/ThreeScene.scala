package RDFExplorer.world

import scala.scalajs.js
import org.denigma.threejs._
import org.denigma.threejs.extensions.Container3D
import org.denigma.threejs.extensions.controls.CameraControls
import RDFExplorer.controls.NavControls

import org.scalajs.dom.raw.HTMLElement
import scala.util.Random

import org.scalajs.dom.html
import org.scalajs.dom
import dom.document

class ThreeScene(val container:HTMLElement, val width:Double, val height:Double) extends Container3D {

  override def distance = 15

  override val controls = new NavControls(camera, this.container)

  Lights(scene);

  val boxGeom = new BoxGeometry(1, 1, 1)

  val projector = new Projector()
  val raycaster = new Raycaster()

  def findIntersections(x:Double, y:Double) = {
    val vector = new Vector3( x, y, 1 )
    projector.unprojectVector( vector, camera )
    raycaster.set( camera.position, vector.sub( camera.position ).normalize() )
    raycaster.intersectObjects( scene.children ).sortWith( (a,b)=>a.point.distanceTo(vector)<b.point.distanceTo(vector)).toList
  }

  val plainMaterial = new MeshLambertMaterial(js.Dynamic.literal(
    color = new Color().setHex(0xffffff)
  ).asInstanceOf[MeshLambertMaterialParameters])

  val space = 1.5
  val meshes:Seq[Mesh] = Range(0, 25).map(i => {

    val mesh = new Mesh(boxGeom, plainMaterial)
    mesh.position.copy(new Vector3(
      Random.nextInt(10) - 5,
      Random.nextInt(10) - 5,
      -5 - Random.nextInt(10)))
    scene.add(mesh)
    mesh

  })

  // Derp, scala.js is forcing me to used LineDashed...
  // https://github.com/antonkulaga/scala-js-facades/issues/2
  val lineMaterial = new LineDashedMaterial(js.Dynamic.literal(
    color = new Color().setHex(0x0088ff)
  ).asInstanceOf[LineDashedMaterialParameters]);

  val lineGeo = new Geometry();
  meshes.foldLeft (meshes(0)) { (ac, el) =>
    // TODO: make right angles to dest.
    lineGeo.vertices.push(el.position.clone());

    el
  }

  val line = new Line(lineGeo, lineMaterial);
  scene.add(line);

  Range(0, 5).map(i => {

    val testText = TextPlane("Test text " + i)
    testText.position.copy(new Vector3(
      Random.nextInt(10) - 5,
      Random.nextInt(10) - 5,
      -5 - Random.nextInt(10)))
    scene.add(testText)

  })

  // Set to width (todo: handle resize)
  camera.aspect = dom.window.innerWidth / height
  camera.updateProjectionMatrix()
  renderer.setSize( dom.window.innerWidth, height );

  def onCursorMove(clientX:Double, clientY:Double, width:Double, height:Double) =
  {
    val mouseX = ( clientX / width) * 2 - 1
    val mouseY = - ( clientY / height ) * 2 + 1

    val intersections = findIntersections(mouseX,mouseY)
    val underMouse = intersections.groupBy(_.`object`).toMap
    //val l = last // if I do not do this assigment and use last instead of l I get into trouble
    underMouse

  }

  override def onEnterFrame() {

    super.onEnterFrame()

    camera.position.z += (Math.sin(java.lang.System.currentTimeMillis() / 800.0) * 0.01)

    meshes(1).rotation.y += 0.01;
    meshes(2).rotation.y += 0.015;


    val hits = onCursorMove(controls.lastMousePos._1, controls.lastMousePos._2, width, height);
    if (!hits.isEmpty) {
      hits.head._1.position.z -= 0.05;//(Random.nextFloat() * 0.2) - 0.1
    }

  }

}