package org.asterope.util

import junit.framework.Assert

class MinimumTest extends ScalaTestCase{

  def testPi() {
    val res = Minimum.find(3, (x=>math.Pi + x * x))
    Assert.assertEquals(math.Pi, res.get, 1e-12);
  }


  def testX() {
    val res = Minimum.findX(-100, (x=>(x+100)*(x+100)))
    Assert.assertEquals(-100, res.get, 1e-12);
  }


  def testSign() {
    val res = Minimum.find(3, (x=>math.signum(x)))
    assert(res.isEmpty,"signum does not have minimum");
  }

}
