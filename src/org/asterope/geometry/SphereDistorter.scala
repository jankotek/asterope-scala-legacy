package org.asterope.geometry

/**This class defines a non-linear distortion in the image plane.
    Normally the forward distortion converts from a fiducial
    projection plane to some distorted coordinates.  The reverse
    distortion transforms from the distorted coordinates back
    to the fiducial coordinates.
 */
abstract class SphereDistorter extends Transformer{

  def getName = "Generic SphereDistorter"

  def getDescription =  "Placeholder for distortions in celestial sphere"

  def inverse: SphereDistorter


  def getOutputDimension = 3

  def getInputDimension = 3
}

