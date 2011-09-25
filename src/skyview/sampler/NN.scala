package skyview.sampler;

/**
 * This class implements a nearest neighbor sampling
 * IS very fast, but does not produce that good results
 */
class NN extends Sampler {

    
    def getName = "NNSampler"

    def getDescription = "Sample using the nearest input pixel value";


    /** Sample at a specified pixel */
    def sample(pix:Int) {
      val tmpOut  = new Array[Double](2)

	    val in = outImage.getCenter(pix);
	    trans.transform(in, tmpOut);

	
	    // Remember that the pixel value is assumed
	    // to be at the center of the pixel not the corner.
	    val x = tmpOut(0).toInt;
	    val y = tmpOut(1).toInt;
	
	    if (x < 0 || x >= inWidth || y < 0 || y >= inHeight) {
	      return;
	    } else {
        var k = 0;
        while(k<inDepth){
		      val v = inImage.getData(x+inWidth*y+k*inWidth*inHeight);
		      outImage.setData(pix+k*outWidth*outHeight, v);
          k+=1
	      }
	    }
    }
    
}
