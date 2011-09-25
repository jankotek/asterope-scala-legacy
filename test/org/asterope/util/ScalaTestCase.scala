package org.asterope.util

import junit.framework._
import scala.math._

/**
 * JUnit test case which mimics Java behaviour. It adds
 * '===', '!==', '~==', '?>' and '?<' operators which shows values of 
 * expressions on both sides.
 * <p>
 * TestCase inherits methods from Assert where are 
 * defined as static. Those are not visible to Scala code,
 * so this class redefines them and calls original static
 * methods.  
 *  
 * @author Jan Kotek
 *
 */
abstract class ScalaTestCase extends TestCase{
	
	/** class used in implicit conversion for === and !== operators */
	protected class Operators(a1:Any){
		def ===(a2:Any):Option[String] = 
			if(a1 == a2) None
			else Some(a1+" != "+a2);
		
		def !==(a2:Any):Option[String] = 
			if(a1 != a2) None
			else Some(a1+" == "+a2);	
			
		def ~==(v2:Double):Option[String] = {
			val v1 = a1.asInstanceOf[Double]
			if(abs(v1-v2)<abs(max(v1,1)/1e12)) return None
			else Some(v1+" not even close to "+v2);
		}
	}
	
	/** implicit conversions which provides === and !== operators */
	implicit def anyToOperators(a1:Any) = new Operators(a1);
	

	/** class which privides ?>, ?< , ?>= and ?<= operators*/
	protected class OrderedOperators[E](a1:Comparable[E]){
		def ?<  (that: E): Option[String] = 
			if(a1.compareTo(that)<0) None 
			else Some(a1+" >= "+that)
  		def ?>  (that: E): Option[String] = 
			if(a1.compareTo(that)>0) None 
			else Some(a1+" <= "+that)

  		def ?<= (that: E): Option[String] = 
			if(a1.compareTo(that)<=0) None 
			else Some(a1+" > "+that)
  		def ?>= (that: E): Option[String] = 
			if(a1.compareTo(that)>=0) None 
			else Some(a1+" < "+that)  					
	}



	/** implicit conversions which provides ?> and ?< operators */
	implicit def  anyToOrderedOperators[E](a1:Comparable[E]) =
			new OrderedOperators[E](a1);


	implicit def  anyToOrderedOperatorsInt(a1:Int) =
			new OrderedOperators[java.lang.Integer](a1);

	implicit def  anyToOrderedOperatorsDouble(a1:Double) =
		new OrderedOperators[java.lang.Double](a1);

	implicit def  anyToOrderedOperatorsFloat(a1:Float) =
		new OrderedOperators[java.lang.Float](a1);

	implicit def  anyToOrderedOperatorsLong(a1:Long) =
		new OrderedOperators[java.lang.Long](a1);


		
	def fail(){
		Assert.fail();
	}
	
	def fail(msg:String){
		Assert.fail(msg);
	}

	def assert(b:Boolean){
    assert(b,"")
  }

	def assert(b:Boolean, msg: => String){
		if(!b) throw new AssertionFailedError(msg);
	}

	
	def assert(error:Option[String]){
		if(error!=None) throw new AssertionFailedError(error.get);
	}

	def assert(error:Option[String], msg:String){
		if(error!=None) throw new AssertionFailedError(error.get+"; "+msg);
	}

  /**
   * Expects exception to be thrown from code block, or fail
   * Usage:
   *    intercept[NullPointerException]{
   *      null.equals(null
   *    }
   *
   *
   */
	def intercept[T <: AnyRef](block: => Any)(implicit manifest: Manifest[T]): Unit = {		
		try{
			block
		}catch {
			case u: Throwable => {
				val clazz = manifest.erasure.asInstanceOf[Class[T]]		
		
				if (!clazz.isAssignableFrom(u.getClass))						
					throw u; //wrong type of exception, rethrow
			}
      }
	}

  /**
   * Expect that block returns given value or fail.
   * Usage:
   *    expect(2){
   *      1+1
   *    }
   */
	def expect(value:Any)(block: => Any){
    assert(value === block)
  }

  def sleep(i:Long){
    Thread.sleep(i)
  }

  /**
   * Wait until conditions became true
   * Periodically query expression, and blocks until it returns true
   * Or throw exception if it takes more than 30 seconds
   */
  def waitUntil(block: =>Boolean, msg:String="Time out!", timeout:Int=30000){
    val start = System.currentTimeMillis
    while(!block){
      sleep(100)
      if(System.currentTimeMillis - start > timeout)
        fail(msg)
    }
  }
}


class ScalaTestCaseExampleTest
      extends ScalaTestCase{  //notice class it extends instead of 'TestCase'

      // first test case demonstrating equal operators
      // note 'test' prefix
      def testOperators{
         val s1 = "123456"
         // !== operator is exactly same as !=,
         // but if both side does not equal
         // it throws exception which shows both values
         assert(s1 !== 123456)

         // === similar as !== operator
         assert(s1 === "123456")

        // ~== compares two double values and pass if are 'nearly' identical
        assert(1d ~== 1d + 1e-15)


        // ?< and ?> operator throw exception with both values, if assertion fails
        assert( 1 ?< 12 )
        assert( 12 ?> 1 )
      }

      //Intercept operation catches expected exceptions
      def testIntercept{
        intercept[NullPointerException]{
          null.equals(null)
        }
      }

    //Expect will throw exception if given block returns other value
    def testExpect{
      expect(1){
        2-1
      }
    }


}


