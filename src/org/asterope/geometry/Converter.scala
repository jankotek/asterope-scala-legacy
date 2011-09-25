package org.asterope.geometry

import collection.mutable.ArrayBuffer

/**A converter applies a succession of transformations on the data */
class Converter(val transforms:List[Transformer]) extends Transformer{

  
  def this(a:Array[Transformer]) = this(a.toList)

   /** optimized version of `transforms` */
  protected val comps:Array[Transformer] = {
    
	val cc = ArrayBuffer[Transformer]()
	cc ++= (transforms.filter(_!=null))
    //start recursive optimalization
    optimizeRecur(cc)
    cc.toArray
  }
  
  //check that dimensions are ok
  comps.reduceLeft{(a1,a2)=>
    require(a1.getOutputDimension == a2.getInputDimension,"output and input dimension does not match.")
  	a2
    }

  /** caches output dimensions */
  protected val compOutDimensionArr:Array[Int] = comps.map(_.getOutputDimension)

  val getInputDimension:Int = comps(0).getInputDimension
  
  val getOutputDimension: Int = comps.last.getOutputDimension
  


  /**Transform a vector */
  def transform(in: Array[Double], out: Array[Double]){

    if (comps.length == 0) {
      if (in != out) {
        System.arraycopy(in, 0, out, 0, in.length)
      }
      return
    }
    var from: Array[Double] = in
    var to: Array[Double] = null
    var oldfrom: Array[Double] = null

    var i: Int = 0
    while (i < comps.length) {
      val t = comps(i)
      val outDim = compOutDimensionArr(i)

      //some harakiri to reuse array instances
      if (i == comps.length - 1)
        to = out  //force usage of 'out' object on last elem
      else if (oldfrom != null && oldfrom.length == outDim)
        to = oldfrom  //reuse previous 'from' array
      else if (outDim == 2)
        to = new Array[Double](2)
      else
        to = new Array[Double](3)
      t.transform(from, to)
      oldfrom = from
      from = to

      i += 1
    }
  }

  def inverse = {
    val rev = comps.reverse.map(_.inverse).toList  
    new Converter(rev);
  }

  def isInverse(t:Transformer):Boolean = {

    // Two null transformations are inverses I suppose!
    if (t == null && comps.size == 0) {
      return true
    }


    // A non-converter can be an inverse if it exactly
    // inverts the single operation
    if (!t.isInstanceOf[Converter]) {
      return comps.size == 1 && comps(0).isInverse(t)
    }
    val c = t.asInstanceOf[Converter]

    if(c.comps.size != comps.size)
      return false;

    // Check component by component
    // Note that this assumes that 'c' is performed after
    // the transformations in 'this'.
    val compsReverse = comps.reverse
    for(i<-0 until comps.size;
      c1 = compsReverse(i);
      c2 = c.comps(i);
      if(!c1.isInverse(c2))
    ){
      return false
    }
    return true
  }

  /** an recursive method to remove uncecessary transformers */
  protected def optimizeRecur(cc:ArrayBuffer[Transformer]){
	
    // We restart the check at the beginning whenever
    // we delete anything, to handle cases like:
    // A * B * C where A*B is the inverse of C -- we
    // want to get rid of all three, and
    // C B A a b c  -> null
    // where a is A inverse, b is B inverse, c is C inverse
    while(cc.size>0){
      var last = cc(0)
      var i = 1

      while (i < cc.size) {
        val curr = cc(i)

        // Check for inverses first so that
        // we delete inverse rotaters and scalers rather
        // than adding them.
        if (last.isInverse(curr)) {
          cc.remove(i)
          cc.remove(i - 1)

          // Start again in case there is a string of inverses to remove.
          optimizeRecur(cc)
          return
        }

        if (last.isInstanceOf[Rotater] && curr.isInstanceOf[Rotater]) {
          val comb = (last.asInstanceOf[Rotater]).add(curr.asInstanceOf[Rotater])

          // Get rid of the two old transformations
          cc.remove(i)
          cc.remove(i - 1)
          cc.insert(i - 1, comb)
          optimizeRecur(cc)
          return
        }
        if (last.isInstanceOf[Scaler] && curr.isInstanceOf[Scaler]) {
          val comb = (last.asInstanceOf[Scaler]).add(curr.asInstanceOf[Scaler])

          // Get rid of the two old transformations
          cc.remove(i)
          cc.remove(i - 1)
          cc.insert(i - 1, comb)
          optimizeRecur(cc)
          return
        }
        last = curr
        i += 1
      
      }
      // If we get this far we are done!
      return;
    }
  }


}