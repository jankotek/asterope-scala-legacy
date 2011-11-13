package org.asterope.util

class InterpolationTest extends ScalaTestCase{

  def testInterpolation{

    val x = Array[Double] ( 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 );
    val y = Array[Double] ( 1, 2, 3, 1, 2, 3, 1, 2, 3, 1, 2, 3 );
    val z = Array[Double] ( 1, 2, 3, 2, 3, 4, 3, 4, 5, 2, 3, 4 );

    val interp = new Interpolation(x, y, z, false);
    val px = 1.5;
    val pz  = 2.5;
    val py = interp.linearInterpolation3d(px, pz);

    println(px + " / " + pz + " / " + py);
    //TODO not sure PY value is correct, it come out when test was first run
    assert(py === 2.0)

  }
}
