/*
 *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */
package org.asterope.util


/**
 * finds local minimum for given function using binary search on derivation
 *
 */
object Minimum {
  /**
   * find minimum for given function
   * @param start:Double start iteration here
   * @param f function which returns Y for given X
   * @return minimum value returned by function (Y), or null if not found
   */
  def find(start: Double, f: (Double)=>Double): Option[Double] = {
    findX(start,f).map(f(_))
  }

  /**
   * find minimum for given function
   * @param start:Double start iteration here
   * @param f function which returns Y for given X
   * @return minimum value passed to function (X), or null if not found
   */
  def findX(start: Double, f: (Double)=>Double): Option[Double] = {
    var counter:Int = 0;
    val derivNearZero = 1e-80

    var step:Double = 1
    var x:Double = start + step;
    var yOld:Double = f(start)
    var y:Double = f(x)
    var deriv:Double = (y-yOld) / step
    //old derivation is here to detect flat functions (such as Signum)
    var oldDeriv:Double = 0;
    while(true){
      //if derivation & step is too small, return result
      if(math.abs(deriv)<derivNearZero && math.abs(oldDeriv)>derivNearZero){
        return Option(x-step/2)
      }
      //flat surface detected, return None
      if(math.abs(deriv)<derivNearZero && math.abs(oldDeriv)<derivNearZero){
        return None
      }
      //if derivation is same as step, reverse and divide step
      if(math.signum(step) == math.signum(deriv)){
        step =  - step/4
      }

      yOld = y;
      oldDeriv = deriv;
      x = x + step;
      y = f(x)
      deriv = (y-yOld) / step
      counter+=1;
      if(counter>=Integer.MAX_VALUE-100) //we dont want an infinitive loop
        throw new InternalError("Find minimum ended in infinitive loop, x="+x)
    }
    return None;
  }


}