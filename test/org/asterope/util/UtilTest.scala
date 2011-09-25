package org.asterope.util


class PredefTest extends ScalaTestCase{

	
	
	def testAngleImport{
		val d1 = 1.degree
		val d2 = (0.5).degree
		assert(d1 === (d2 * 2))
	}
	
	def testVector{
		val v = Vector3d(1,1,1)
		assert(v.normalized.length === 1d)
	}

}
