package skyview.survey

import org.asterope.util._
import org.asterope.chart.Chart
import java.io.File

class DSSDistorterTest extends ScalaTestCase {

    
  
  def testDSSDistorter(){
        val d = new DSSDistorter(67.2,        
        Array(67.232604454256, -0.0056713130443136, 0.45387404253871, -1.1736687474467E-5, 3.4562764994183E-6, 7.5639518639913E-6, 0.0, 1.8261922043364E-6, 3.2782813629106E-7, 2.2738238339073E-6, 2.5404825637623E-7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        Array(67.23027634436, -0.010281346232148, -2.3970106786322, 1.6578263780683E-5, -4.0827591099514E-6, 6.0928315637203E-7, 0.0, 2.2213277691401E-6, 3.8769624259009E-7, 1.7838793727058E-6, 2.8097266515556E-7, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)        
        )
    val res = new Array[Double](2)
    d.transform(Array(0D,0D),res)
    assert(res(0) === -2.19839658150788E-6)
    assert(res(1) === 1.161546601194527E-5)
    val res2 = new Array[Double](2)
    d.inverse.transform(res,res2)
    assert(res2(0) ~== 0.0)
    assert(res2(1) ~== 0.0)
    
  }
  

}