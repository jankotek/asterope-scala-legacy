
package skyview.sampler;

/**
 * This class implements a Lanczos scheme.
 * Is slow, but produces great results on well sampled data.
 *
 * @param nLobe The number of lobes in the window, default 3
 *
 */
class Lanczos(nLobe:Int=3) extends Sampler {
    
    
    def getName =  "Lanczos"+nLobe+" Sampler";

    
    def getDescription =  "Sample using smoothly truncated sinc kernel";

    private val coef = math.Pi/nLobe
    private val coef2 = coef * math.Pi
    

    def sample(pix:Int){
      val out = new Array[Double](2)

      /**
       * Weights used internally
       * TODO allocating new array each time may consume too much CPU
       */

      val xw = new Array[Double](nLobe * 2);
      val yw = new Array[Double](nLobe * 2);


  	  var output = 0.0;
      val in = outImage.getCenter(pix);
	    trans.transform(in, out);
	
	    val x = out(0)-0.5;
	    val y = out(1)-0.5;

      val ix = math.floor(x).toInt;
      val iy = math.floor(y).toInt;
	
	    var dx = ix - x - (nLobe-1);
	    var dy = iy - y - (nLobe-1);

  	  if (ix < nLobe-1 || y < nLobe-1 || ix >= inWidth-nLobe || iy >= inHeight-nLobe){
	      return;

	    } else {
        var xc = 0;
	      while (xc < 2*nLobe) {
		      if (math.abs(dx) < 1.e-10) {
		        xw(xc) = 1;
		      } else {
		        xw(xc) = math.sin(coef*dx)*math.sin(math.Pi*dx)/(coef2*dx*dx);
		      }
		      dx += 1;
          xc += 1
	      }

        var yc = 0;
	      while (yc < 2*nLobe) {
		      if (math.abs(dy) < 1.e-10) {
		        yw(yc) = 1;
		      } else {
		        yw(yc) = math.sin(coef*dy)*math.sin(math.Pi*dy)/(coef2*dy*dy);
		      }
		      dy += 1;
          yc += 1
	      }

        var k = 0;
	      while (k<inDepth) {
		      var p  = (iy-(nLobe-1))*inWidth + ix-(nLobe-1) + k*inWidth*inHeight;

          yc = 0;
		      while (yc<2*nLobe) {
            xc = 0
		        while (xc<2*nLobe) {
			        output += inImage.getData(p)*xw(xc)*yw(yc);
			        p += 1;
              xc += 1
		        }
		        p += inWidth - 2*nLobe;
            yc += 1
		      }
          k += 1
	      }
	    }
	  outImage.setData(pix, output);
  }
}

