package org.asterope.util

import scala.math._

class Vector3dTest extends ScalaTestCase{

  def testEqual(){
    assert (Vector3d(1,1,1) == Vector3d(1,1,1))
  }

  def testHash(){
    assert(Vector3d(1,1,1).hashCode === Vector3d(1,1,1).hashCode);
    assert(Vector3d(1,1,1).hashCode != Vector3d(1,1,1.1).hashCode);
  }

  def testLength() {
    assert (1d === Vector3d(1d,0d,0d).length)
    assert(sqrt(2d) === Vector3d(1d,1d,0d).length);
    assert(sqrt(12d) === Vector3d(2d,2d,2d).length);
  }

  def testLengthMultiply(){
    assert(Vector3d(2d,0d,0d) === Vector3d(1d,0d,0d).lengthMultiply(2))
  }

  def testDot(){
    assert ( Vector3d(2,4,0).dot(Vector3d(1,5,0)) === 22)
    assert ( Vector3d(2,3,5).dot(Vector3d(4,-2,-1)) === -3)
  }

  def testCross(){
    assert ( Vector3d(2,1,-3).cross(Vector3d(0,4,5)) === Vector3d(17,-10,8))
  }

  def testAngle(){
    assert (Vector3d(1,0,0).angle(Vector3d(0,1,0)) === Pi/2);
  }

  def testAdd(){
    assert(Vector3d(5,4,3).add(Vector3d(1,2,3)) === Vector3d(6,6,6));
  }
  
  def testToArray(){
	val v = Vector3d(4,5,6);
	val ar = v.toArray;
	assert (v.x === ar(0));
	assert (v.y === ar(1));
	assert (v.z === ar(2));
  }

}