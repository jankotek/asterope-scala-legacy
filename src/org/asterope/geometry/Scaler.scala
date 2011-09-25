package org.asterope.geometry

import java.lang.Math.abs
import java.io.PrintStream

/**This class does 2-D scalings, rotations and linear transformations.
 * Create a scaler where y0 = x0 + a00*x0 + a01*x1, y1 = x0+a10*x0+a11*x1.
 * @param x0  The X offset
 * @param y0  The Y offset
 * @param a00 Coefficient of the transformation matrix.
 * @param a01 Coefficient of the transformation matrix.
 * @param a10 Coefficient of the transformation matrix.
 * @param a11 Coefficient of the transformation matrix.
 */
case class Scaler(x0: Double, y0: Double, a00: Double, a01: Double, a10: Double, a11: Double) extends Distorter{

  def getParams: Array[Double] = {
    return Array[Double](x0, y0, a00, a01, a10, a11)
  }


  /**Scale a single point where the user supplies the output.
   * @param x The input point (should be double[2])
   * @param y The output point (should be double[2])
   */
  def transform(x: Array[Double], y: Array[Double]){
    var t: Double = .0
    t = x0 + a00 * x(0) + a01 * x(1)
    y(1) = y0 + a10 * x(0) + a11 * x(1)
    y(0) = t
  }

  /**
   * Return the inverse transformation.
   * @return A transformation object that scales in the opposite direction.
   * @throws TransformationException if the forward transformation matrix is singular.
   */
  def inverse: Scaler = {
    var sum: Double = abs(a00) + abs(a01) + abs(a10) + abs(a11)
    if (sum == 0) {
      throw new IllegalArgumentException("Zero matrix in Scaler")
    }
    var det: Double = a00 * a11 - a01 * a10
    if (det == 0) {
      throw new IllegalArgumentException("Non-invertible transformation in Scaler")
    }
    if (abs(det) / abs(sum) < 1.e-10) {
      System.err.println("Scaler transformation is likely not invertible")
    }
    return new Scaler(-x0 * a11 / det + y0 * a01 / det,
      x0 * a10 / det - y0 * a00 / det,
      a11 / det, -a01 / det, -a10 / det, a00 / det)
  }

  /**
   * Add a second affine transformation to this one and return the composite
   * transformation.
   * @param trans	A second transformation which is applied after the transformation
   * described in 'this'.
   * @return The combined transformation.
   */
  def add(trans: Scaler): Scaler = {
    if (trans == null)
      this
    else
     new Scaler(trans.x0 + trans.a00 * x0 + trans.a01 * y0,
       trans.y0 + trans.a10 * x0 + trans.a11 * y0,
       trans.a00 * this.a00 + trans.a01 * this.a10,
       trans.a00 * this.a01 + trans.a01 * this.a11,
       trans.a10 * this.a00 + trans.a11 * this.a10,
       trans.a10 * this.a01 + trans.a11 * this.a11)
  }

  /**Is this an inverse of the current scaler? */
  def isInverse(trans: Transformer): Boolean = {
    if (!(trans.isInstanceOf[Scaler])) {
      return false
    }
    var sum: Scaler = add(trans.asInstanceOf[Scaler])
    return sum.isUnit
  }

  /**What is the scale of this transformation? This is defined as
   *  the ratio of the lengths between a unit transformation on input.
   *  and the output.
   */
  def scale: Double = {
    return math.sqrt((a00 + a01) * (a00 + a01) + (a10 + a11) * (a10 + a11)) / math.sqrt(2)
  }

  /**Interchange the X and Y axes */
  def interchangeAxes = new Scaler(y0,x0,a10,a00,a11,a01)

  /**Is this a unit scaler? */
  private def isUnit: Boolean = {
    return (abs(x0) + abs(y0) + abs(a01) + abs(a10) + abs(1 - a00) + abs(1 - a11)) < 1.e-10
  }

}

