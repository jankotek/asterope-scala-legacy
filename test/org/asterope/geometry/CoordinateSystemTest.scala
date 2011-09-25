package org.asterope.geometry;

import org.asterope.util.ScalaTestCase

class CoordinateSystemTest extends ScalaTestCase{

  def a(r:Rotater, x:Double,y:Double, z:Double, x2:Double, y2:Double, z2:Double){
    val res = r.transform(Array[Double](x,y,z))
    assert(res(0) ~== x2)
    assert(res(1) ~== y2)
    assert(res(2) ~== z2)
  }


  def testEcliptic(){
    val cs =  CoordinateSystem.factory("Ecliptic").getRotater
    a(cs,1,0,0,0.8843379633847844,-0.46684726194410375,-2.3059594739721634E-5)

  }
}