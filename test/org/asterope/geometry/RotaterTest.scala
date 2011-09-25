package org.asterope.geometry

import org.asterope.util._

class RotaterTest extends ScalaTestCase {

  def a(r:Rotater, x:Double,y:Double, z:Double, x2:Double, y2:Double, z2:Double){
    val res = r.transform(Array[Double](x,y,z))
    assert(res(0) ~== x2)
    assert(res(1) ~== y2)
    assert(res(2) ~== z2)
  }

  def testXAxis(){
    val r1 = Rotater("x",90 * Angle.D2R, 0 ,0)
    a(r1, 1,0,0, 1,0,0)
    a(r1, 0,1,0, 0,0,-1)
    a(r1, 0,0,1, 0,1,0)
  }
  def testYAxis(){
    val r1 = Rotater("y",90 * Angle.D2R, 0 ,0)
    a(r1, 1,0,0, 0,0,1)
    a(r1, 0,1,0, 0,1,0)
    a(r1, 0,0,1, -1,0,0)
  }
  def testZAxis(){
    val r1 = Rotater("z",90 * Angle.D2R, 0 ,0)
    a(r1, 1,0,0, 0,-1,0)
    a(r1, 0,1,0, 1,0,0)
    a(r1, 0,0,1, 0,0,1)
  }

  def testMultiAxisRotation(){
    val r = Rotater("xyz",48 *Angle.D2R, 48 *Angle.D2R, 48 *Angle.D2R)
    a(r, 1,0,0,  0.4477357683661733, -0.4972609476841367, 0.7431448254773942)

    val v = Vector3d.rade2Vector(1,-1)
    a(r,v.x,v.y,v.z, 0.3400649326435842,-0.8575798632289657,-0.3858919794065476)

  }


}