package org.asterope.data.catalog

import java.io._
import org.asterope.util._

class Ucac3CatalogTest extends ScalaTestCase{
    
    val catalog = new Ucac3Catalog()
    
    def testReadSize{
        val in = new DataIS(new ByteArrayInputStream(new Array[Byte](1000)))
        val avail1  = in.available();
        val star = new Ucac3BinaryEntry(in)
        val readed = avail1 - in.available();
        assert(readed === 84)
    }





    def testReadBinaryData{

      //test data compared to first entry from 'out.sam', file with sample output

      val buf = Array[Byte](102,99,63,77,7,-39,79,19,30,62,3,62,-58,0,0,0,54,0,47,0,3,3,2,2,-8,37,68,38,-93,0,0,0,5,0,0,0,113,0,108,0,-20,80,-95,78,-35,51,56,49,-121,48,5,5,5,3,3,3,119,69,-34,63,86,59,11,1,1,1,0,0,0,0,0,0,0,0,1,0,0,0,0,0,47,-34,-86,4)

      val binary = new Ucac3BinaryEntry(new DataIS(new ByteArrayInputStream(buf)))

      assert(math.abs(binary.ra-359.9999574.degree.toMas)< 1000)
      assert(math.abs(binary.spd - 90.degree.toMas - 00.0000020.degree.toMas)< 1000)

      assert(binary.im1 === 15902)
      assert(binary.im2 === 15875)
      assert(binary.sigmag === 198)

      assert(binary.objt === 0)
      assert(binary.dsf === 0)
      assert(binary.sigra === 54)
      assert(binary.sigdc === 47)

      assert(binary.na1 === 3)
      assert(binary.nu1 === 3)
      assert(binary.us1 === 2)
      assert(binary.cn1 === 2)

      assert(binary.pmrac === 163)
      assert(binary.pmdc === 5)
      assert(binary.sigpmr === 113)
      assert(binary.sigpmd === 108)

      assert(binary.id2m === 1319194860)
      assert(binary.jmag === 13277)
      assert(binary.hmag === 12600)
      assert(binary.kmag === 12423)

      assert(binary.smB === 17783)
      assert(binary.smR2 === 16350)
      assert(binary.smI === 15190)

      assert(binary.leda === 0)
      assert(binary.x2m === 0)

//#3UC      |    RA  (ICRS) Dec     +/- +/-  mas  EpRA    EpDE  | f.mag  a.mag  +/- |ot d| Na  Nu  Ca  Cu| pmRA(mas/yr)pmDE  +/-  +/-|MPOS     |     2Mkey   Jmag  +/-:cq   Hmag  +/-:cq   Kmag  +/-:cq|sc   Bmag:q   R2mag:q    Imag:q |HTAbhZBLSY.gc|LED 2MX
//181-285554|359.9999574+00.0000020  54  47   81 1997.20 1997.96|15.902 15.875 0.198| 0 0|  3   3   2   2|   +16.3      +.5 11.3 10.8|078306863|1319194860 13.277 0.03:05 12.600 0.03:05 12.423 0.03:05|11 17.783:01 16.350:01 15.190:01|0000000010.00|  0   0
//               x            x      x    x                         x     x       x   x x   x   x   x   x      x          x   x   x                  x        x             x               x                   x       x         x                      x   x

    }
}