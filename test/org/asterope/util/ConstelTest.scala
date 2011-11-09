package org.asterope.util

import org.apache.commons.math.geometry.Vector3D


class ConstelTest extends ScalaTestCase{
	
	def testAbreviationRegExp{
		assert("Vul".matches(Constel.abreviationRegExp))
		assert("And".matches(Constel.abreviationRegExp))
		assert(!"Aaa".matches(Constel.abreviationRegExp))
	}
	



  def testConstelOnPosition{
    assert(Constel.constelOnPosition(Vector3D.PLUS_K) === Constel.UMi)
    assert(Constel.constelOnPosition(Vector3D.MINUS_K) === Constel.Oct)
    assert(Constel.constelOnPosition(Vector3D_asterope) === Constel.Tau)
    assert(Constel.constelOnPosition(Vector3D_galaxyCentre) === Constel.Sgr)
    assert(Constel.constelOnPosition(Vector3D_m31) === Constel.And)

    val v1 = rade2Vector(Angle.parseRa("08h10m41.753s").toRadian,Angle.parseDe("+59°43'02.67").toRadian)
    assert(Constel.constelOnPosition(v1) ===Constel.UMa)
  }

}