package org.asterope.util


class ConstelTest extends ScalaTestCase{
	
	def testAbreviationRegExp{
		assert("Vul".matches(Constel.abreviationRegExp))
		assert("And".matches(Constel.abreviationRegExp))
		assert(!"Aaa".matches(Constel.abreviationRegExp))
	}
	



  def testConstelOnPosition{
    assert(Constel.constelOnPosition(Vector3d.northPole) === Constel.UMi)
    assert(Constel.constelOnPosition(Vector3d.southPole) === Constel.Oct)
    assert(Constel.constelOnPosition(Vector3d.asterope) === Constel.Tau)
    assert(Constel.constelOnPosition(Vector3d.galaxyCentre) === Constel.Sgr)
    assert(Constel.constelOnPosition(Vector3d.m31) === Constel.And)

    val v1 = Vector3d.rade2Vector(Angle.parseRa("08h10m41.753s"),Angle.parseDe("+59°43'02.67"))
    assert(Constel.constelOnPosition(v1) ===Constel.UMa)
  }

}