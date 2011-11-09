package org.asterope.util


class PredefTest extends ScalaTestCase{

	
	
	def testAngleImport{
		val d1 = 1.degree
		val d2 = (0.5).degree
		assert(d1 === (d2 * 2))
	}
	

}
