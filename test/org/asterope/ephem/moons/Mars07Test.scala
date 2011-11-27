package org.asterope.ephem.moons

import org.asterope.util.ScalaTestCase

//TODO not sure about values, need to verify in real
class Mars07Test extends ScalaTestCase{

 val jd = 2451605.00;
 def testPhobos{
   val l1 = List(
    3.391006237566556E-5,
    -3.851652620273116E-5,
    -3.7325256225233724E-5,
    8.709008728560487E-4,
    8.50538844319611E-4,
    -6.895138014801465E-5)

   val pos = Mars07.getMoonPosition(jd, Mars07.SATELLITE_PHOBOS, Mars07.OUTPUT_POSITIONS);
   l1.zipWithIndex.foreach{case(v,i)=>
     assert(v ~== pos(i))
   }

 }

 def testDeimos{
   val l2 = List(
   -8.86364950412834E-5,
   8.725294013740733E-5,
   9.554029153719937E-5,
   -5.341970935406236E-4,
   -5.682249168849269E-4,
   2.3172688954810232E-5)

   val pos = Mars07.getMoonPosition(jd, Mars07.SATELLITE_DEIMOS, Mars07.OUTPUT_POSITIONS);
   l2.zipWithIndex.foreach{case (v,i)=>
     assert(v ~== pos(i))
   }


 }



}
