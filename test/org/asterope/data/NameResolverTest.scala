package org.asterope.data

import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

class NameResolverTest extends BeansTestCase{



  def resolve(name:String) = {
    val l = beans.nameResolver.resolve(name)
    assert(l.pos.isDefined,"no object found for name "+name)
    l
  }

  def testObjects(){
    val m13 = resolve("M13")
    assert(Vector3D.angle(m13.pos.get,Vector3D_m13) < 1 * Angle.D2R)
    assert(m13.description.get.toLowerCase.contains("globular cluster"))

    val m31 = resolve("M31")
    assert(Vector3D.angle(m31.pos.get,Vector3D_m31) < 1 * Angle.D2R)
    assert(m31.description.get.toLowerCase.contains("galaxy"))

    val m45 = resolve("M45")
    assert(Vector3D.angle(m45.pos.get,Vector3D_asterope) < 1 * Angle.D2R)
    assert(m45.description.get.toLowerCase.contains("cluster"))

  }

  def testStarts(){
    val asterope = resolve("Asterope")
    assert(Vector3D.angle(asterope.pos.get,Vector3D_asterope) < 1 * Angle.D2R)
    assert(asterope.description.get.toLowerCase.contains("star"))

    val ss = resolve("ξ And")
    assert(ss.description.get.toLowerCase.contains("star"))
    assert(Constel.constelOnPosition(ss.pos.get) === Constel.And)

  }
}