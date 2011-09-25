package org.asterope.util



class UtilsTest extends ScalaTestCase{

  def testBitsEncode{
    val l = List(true,false,true,false,true,true,false,false)
    val b = IOUtil.bitsEncode(l)
    assert(l === IOUtil.bitsDecode(b))

    val l2 = List(true,false,true,true,true)
    val l3 = List(true,false,true,true,true,false,false,false)

    assert(IOUtil.bitsDecode(IOUtil.bitsEncode(l2))==l3)

  }

}