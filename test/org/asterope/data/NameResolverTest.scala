package org.asterope.data

import org.asterope.util._

class NameResolverTest extends ScalaTestCase
  with DataBeans with TestRecordManager{




  def resolve(name:String) = {
    val l = nameResolver.resolve(name)
    assert(l.pos.isDefined,"no object found for name "+name)
    l
  }

  def testObjects(){
    val m13 = resolve("M13")
    assert(m13.pos.get.angle(Vector3d.m13) < 1 * Angle.D2R)
    assert(m13.description.get.toLowerCase.contains("globular cluster"))

    val m31 = resolve("M31")
    assert(m31.pos.get.angle(Vector3d.m31) < 1 * Angle.D2R)
    assert(m31.description.get.toLowerCase.contains("galaxy"))

    val m45 = resolve("M45")
    assert(m45.pos.get.angle(Vector3d.asterope) < 1 * Angle.D2R)
    assert(m45.description.get.toLowerCase.contains("cluster"))

  }

  def testStarts(){
    val asterope = resolve("Asterope")
    assert(asterope.pos.get.angle(Vector3d.asterope) < 1 * Angle.D2R)
    assert(asterope.description.get.toLowerCase.contains("star"))

    val ss = resolve("ξ And")
    assert(ss.description.get.toLowerCase.contains("star"))
    assert(Constel.constelOnPosition(ss.pos.get) === Constel.And)

  }
}