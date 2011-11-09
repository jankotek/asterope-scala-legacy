package org.asterope.geometry

import Jama.Matrix
import java.lang.Math._
import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

/**
     *  Form a rotation from the Euler angles - three successive
     *  rotations about specified Cartesian axes
     *  <p>
     *  A rotation is positive when the reference frame rotates
     *  anticlockwise as seen looking towards the origin from the
     *  positive region of the specified axis.
     *  <p>
     *  The characters of ORDER define which axes the three successive
     *  rotations are about.  A typical value is 'ZXZ', indicating that
     *  RMAT is to become the direction cosine matrix corresponding to
     *  rotations of the reference frame through PHI radians about the
     *  old Z-axis, followed by THETA radians about the resulting X-axis,
     *  then PSI radians about the resulting Z-axis.
     *  <p>
     *  The axis names can be any of the following, in any order or
     *  combination:  X, Y, Z, uppercase or lowercase, 1, 2, 3.  Normal
     *  axis labelling/numbering conventions apply;  the xyz (=123)
     *  triad is right-handed.  Thus, the 'ZXZ' example given above
     *  could be written 'zxz' or '313' (or even 'ZxZ' or '3xZ').  ORDER
     *  is terminated by length or by the first unrecognized character.
     *  <p>
     *  Fewer than three rotations are acceptable, in which case the later
     *  angle arguments are ignored.  If all rotations are zero, the
     *  identity matrix is produced.
     *  <p>
     *  @author Adapted from P.T.Wallace's   SLA routine Deuler
     *
     *  @param 		order   specifies about which axes the rotations occur
     *  @param  	phi     1st rotation (radians)
     *  @param          theta   2nd rotation (   "   )
     *  @param          psi     3rd rotation (   "   )
     *
     *  @return         The corresponding rotation matrix.
     */
class Rotater(matrix2:Matrix) extends Transformer{

  assert(matrix2.getRowDimension == 3 && matrix2.getColumnDimension == 3)

  //make defensive copy of argument, so this class is immutable
  protected val matrix = matrix2.copy

  def isInverse(trans: Transformer): Boolean = {
      if (!(trans.isInstanceOf[Rotater])) {
        return false
      }
      val sum: Rotater = add(trans.asInstanceOf[Rotater])
      return sum.isUnit
  }


  private def isUnit: Boolean = {
      val delta =
        abs(1 - matrix.get(0,0)) + abs(1 - matrix.get(1,1)) + abs(1 - matrix.get(2,2)) +
          abs(matrix.get(0,1)) + abs(matrix.get(0,2)) + abs(matrix.get(1,0)) +
          abs(matrix.get(1,2)) + abs(matrix.get(2,0)) + abs(matrix.get(2,1))

      return delta < 1.e-10
  }


  def getInputDimension = 3
  def getOutputDimension = 3

  def getName =  "Rotater"
  def getDescription = "An object that rotates 3-d vectors in space."

  /**Get the transpose of the Matrix.  For rotation
     *  matrices, the transpose is the inverse.  This
     *  uses a create-on-demand protocol which creates
     *  the transpose matrix on the first transpose call
     *  and simply returns the reference in later calls.
     */
  protected lazy val transpose:Rotater = {
    new Rotater(matrix.transpose)
  }

  def inverse = transpose

    /** Add an additional rotation to the current rotation.
     *  The current rotation is applied first, and then the
     *  additional rotation.  This is equivalent to multiply
     *  the old matrix by the new matrix with new matrix on the left.
     */
  def add(r:Rotater):Rotater = {
    val m2 = r.matrix.times(this.matrix)
    return new Rotater(m2)
  }


  def transform(in:Array[Double], out:Array[Double]){
    //TODO is called even if rotation is 0 degree, optimize it
    /**Multiple a vector by the matrix.
     * Done here so 'out' is reused
     */
    var i: Int = 0
    while (i < in.length) {
      out(i) = 0
      var j: Int = 0
      while (j < matrix.getColumnDimension) {
        out(i) += matrix.get(i,j) * in(j)
        j += 1
      }
      i += 1
    }
  }

  def transform(pos:Vector3D):Vector3D = {
    val v = transform(pos.toArray)
    new Vector3D(v(0),v(1),v(2))
  }

  def printOut(){
    matrix.print(20,20)
  }
}

object Rotater{

  def apply(order:String, ang1:Double,ang2:Double,ang3:Double) = {
    var matrix = Matrix.identity(3,3)
    assert(order.size<=3,"too long order string")
    for(i<-0 until order.size;
      axis = order(i)){
      //initialize rotation matrix
      val rotn = Matrix.identity(3,3)

      // take sine & cosine
      val angle = i match{
        case 0=>ang1; case 1=>ang2; case 2=>ang3;
      }
      val s = math.sin(angle)
      val c = math.cos(angle)

      //Identify the axis and apply sine and cosine
      if (axis == 'x' || axis == 'X' || axis == '1') {
        rotn.set(1,1,c)
        rotn.set(1,2,s)
        rotn.set(2,1,-s)
        rotn.set(2,2,c)
      }else if (axis == 'y' || axis == 'Y' || axis == '2') {
        rotn.set(0,0,c)
        rotn.set(0,2,-s)
        rotn.set(2,0,s)
        rotn.set(2,2,c)
      }else if (axis == 'z' || axis == 'Z' || axis == '3') {
        rotn.set(0,0,c)
        rotn.set(0,1,s)
        rotn.set(1,0,-s)
        rotn.set(1,1,c)
      }else{
        throw new IllegalArgumentException("Unknown axis: "+axis)
      }

      //  Apply the current rotation (matrix ROTN x matrix RESULT)
      matrix = rotn.times(matrix)
    }
    new Rotater(matrix)
  }
}