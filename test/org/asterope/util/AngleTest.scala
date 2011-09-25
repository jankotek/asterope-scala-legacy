package org.asterope.util


class AngleTest extends ScalaTestCase{

  def testParseRaDe{
    import Angle._

    assert(
      (Angle.parseRa("20 54 05.689"),Angle.parseDe("+37 01 17.38"))
      ===
      Angle.parseRaDe("20 54 05.689 +37 01 17.38"))
    assert(
      (Angle.parseRa("10:12:45.3"),Angle.parseDe("-45:17:50"))
      ===
      Angle.parseRaDe("10:12:45.3-45:17:50"))
    assert(
      (Angle.parseRa("15h17m"),Angle.parseDe("-11d10m"))
      ===
      Angle.parseRaDe("15h17m-11d10m"))

    assert(
      (Angle.parseRa("15h17m"),Angle.parseDe("-11d10m"))
      ===
      Angle.parseRaDe("15h17-11d10"))

    assert(
      (Angle.parseRa("275d11m15.6954s"),Angle.parseDe("+17d59m59.876s"))
      ===
      Angle.parseRaDe("275d11m15.6954s+17d59m59.876s"))

    assert(
      (12.34567.arcHour, -17.87654d.degree)
      ===
      Angle.parseRaDe("12.34567h-17.87654d"))

    assert(
      (350.123456.degree, -17.33333.degree)
      ===
      Angle.parseRaDe("350.123456d-17.33333d"))

    assert(
      (350.123456.degree,-17.33333.degree)
      ===
      Angle.parseRaDe("350.123456 -17.33333"))

  }

    def testParse: Unit = {
      assert(Angle.parseRa("1", "2", "3").uas === 1l * 15l * 60l * 60l * 1000l * 1000l + 2l * 15l * 60l * 1000l * 1000l + 3l * 15l * 1000l * 1000l)
      assert(Angle.parseDe("+", "1", "2", "3").uas === 1l * 60l * 60l * 1000l * 1000l + 2l * 60l * 1000l * 1000l + 3l * 1000l * 1000l)
    }

    def testParse2: Unit = {
      assert(Angle.parseRa("1h2m3s").uas === 1l * 15l * 60l * 60l * 1000l * 1000l + 2l * 15l * 60l * 1000l * 1000l + 3l * 15l * 1000l * 1000l)
      assert(Angle.parseRa("02 51.2").uas === 2l * 15l * 60l * 60l * 1000l * 1000l + 512l * 15l * 60l * 1000l * 100l)
      assert(Angle.parseDe("+1d2'3\"").uas === 1l * 60l * 60l * 1000l * 1000l + 2l * 60l * 1000l * 1000l + 3l * 1000l * 1000l)
      assert(Angle.parseDe("-1d2'3\"").uas === -(1l * 60l * 60l * 1000l * 1000l + 2l * 60l * 1000l * 1000l + 3l * 1000l * 1000l))
      assert(Angle.parseDe("+13 12").uas === 13l * 60l * 60l * 1000l * 1000l + 12l * 60l * 1000l * 1000l)
    }
    
  def testConversion(){
    assert(Angle.D2R * 1d === math.toRadians(1d))
    assert(Angle.R2D * 1d === math.toDegrees(1d))
    assert(Angle.H2D * 1d === 15d)
    assert(Angle.D2H * 1d === 1d / 15d)
    assert(Angle.D2M === 60d)
    assert(Angle.M2D === 1d / 60d)
    assert(Angle.D2S === 3600d)
    assert(Angle.S2D === 1d / 3600d)
    assert(Angle.H2R * 1d === math.toRadians(15d))
    assert(Angle.R2H * math.toRadians(15d) === 1d)
    assert(Angle.M2R * 60d === math.toRadians(1d))
    assert(Angle.R2M * math.toRadians(1d) == 60d)
    assert(Angle.Mas2R === Angle.D2R / 3600000d)
    assert(Angle.R2Mas === 1d / Angle.Mas2R)
  }
  
  def testDistance(){
    assert(Angle.distance(Angle.D2R * 1d, 0d, Angle.D2R * 2d, 0d) === Angle.D2R * 1d)
    assert(Angle.distance(0, Angle.D2R * 90d, Angle.D2R * 180d, -(Angle.D2R * 90d)) === Angle.D2R * 180d)
  }

  def testRaToString(){
    assert("11h" === Angle.raToString(Angle.H2R * 11))
    assert("11h 12m" === Angle.raToString(Angle.H2R * 11 + Angle.H2R * 12 / 60))
    assert("11h 12m 13s" === Angle.raToString(Angle.H2R * 11 + Angle.H2R * 12 / 60 + Angle.H2R * 13 / 3600))
    assert("11h 12m 13.3s" === Angle.raToString(Angle.H2R * 11 + Angle.H2R * 12 / 60 + Angle.H2R * 13.3 / 3600))
  }

  def testDeToString(){
    assert("11" + Angle.DEGREE_SIGN === Angle.deToString(Angle.D2R * 11))
    assert("11" + Angle.DEGREE_SIGN + "12'" === Angle.deToString(Angle.D2R * 11 + Angle.M2R * 12))
    assert("11" + Angle.DEGREE_SIGN + "12'13\"" === Angle.deToString(Angle.D2R * 11 + Angle.M2R * 12 + Angle.S2R * 13))
    assert("11" + Angle.DEGREE_SIGN + "12'13.3\"" === Angle.deToString(Angle.D2R * 11 + Angle.M2R * 12 + Angle.S2R * 13.3))
    assert("-11" + Angle.DEGREE_SIGN + "12'" === Angle.deToString(-(Angle.D2R * 11 + Angle.M2R * 12)))
  }
  
    
}