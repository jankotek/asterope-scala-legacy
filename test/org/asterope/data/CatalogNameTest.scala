//package org.asterope.data
//
//import org.asterope.ScalaTestCase
//
//class CatalogNameTest extends ScalaTestCase{
//
//	def testJ2000{
//		val ids = List("J000034.34-530551.7", "J000038.12-664059.4",
//				"J000049.68+364648.3", "J000123.66+003638.5")
//
//		for(id<-ids){
//			assert(CatalogNameUtils.nameIsJ2000Format(id),"not in format: "+id)
//			val id2 = "SKY2000 "+id;
//			assert(id2 === CatalogNameUtils.nameToNameJ2000("SKY2000", id, CatalogNameType.WELL_KNOWN_CAT).name)
//		}
//	}
//
//  def testIntPattern{
//    assert(CatalogNameUtils.INT_PATTERN.pattern.matcher("11112").matches)
//    assert(CatalogNameUtils.INT_PATTERN.pattern.matcher("+111120").matches)
//  }
//
//  def testVarPattern{
//    assert(CatalogNameUtils.VAR_PATTERN.pattern.matcher("V01 Ori").matches)
//    assert(CatalogNameUtils.VAR_PATTERN.pattern.matcher("Z Ori").matches)
//    assert(CatalogNameUtils.VAR_PATTERN.pattern.matcher("ZX UMa").matches)
//  }
//
//}