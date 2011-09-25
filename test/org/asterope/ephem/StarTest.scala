package org.asterope.ephem

import org.asterope.util.ScalaTestCase

class StarTest extends ScalaTestCase{

  def testAbsMag{
    val sunAbsMag: Double = Star.absoluteMagnitude(-26.71, EphemConstant.AU * 1000.0 / EphemConstant.PARSEC)
    println("sunAbsMag: " + sunAbsMag)
    assert(sunAbsMag === 4.8621256654560305)
  }

  def testStellarWindMas{
    val wind = Star.stellarWindMass(7, 500, 1)
    println("solar wind mass lose ratio: " + wind)
    assert(wind === 5.225791290425103E-14)

  }

  def testLuminosity{
    val r: Double = Star.getStarLuminosity(1.7, 9000)
    println("lum " + r)
    assert(r === 17.102179383444803)
  }

  def testBlackBody{
    val bb: Double = Star.blackBody(30, 70.0 * 1.0E-6)
    val k: Double = 0.2
    val dist: Double = 800
    val v : Double = bb * EphemConstant.SUN_MASS * 1000.0 * 1.0E-7 * k / math.pow(dist * EphemConstant.PARSEC * 100.0, 2.0)
    var expectedMass: Double = 0.077 * 1.0E-7 / v
    System.out.println("30K, 70 micras: " + bb + " / " + v + " - " + expectedMass)
    assert(bb === 1.2268346552110973E8)
    assert(v === 8.008520316634428E-10 )
    assert(expectedMass === 9.614759900160829)
  }
}
