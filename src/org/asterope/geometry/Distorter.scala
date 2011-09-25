package org.asterope.geometry

/**This class defines a non-linear distortion in the image plane.
    Normally the forward distortion converts from a fiducial
    projection plane to some distorted coordinates.  The reverse
    distortion transforms from the distorted coordinates back
    to the fiducial coordinates.
 */
abstract class Distorter extends Transformer{

  def getName =  "Generic Distorter"

  def getDescription =  "Placeholder for distortions in projection plane"


  def inverse: Distorter


  def getOutputDimension = 2

  def getInputDimension: Int = 2
}

