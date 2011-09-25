package org.asterope.geometry


/**This class deprojects a point from a projection plane
 *  onto the celestial sphere.
 */
abstract class Deprojecter extends Transformer {

  def getOutputDimension = 3

  def getInputDimension = 2
}

