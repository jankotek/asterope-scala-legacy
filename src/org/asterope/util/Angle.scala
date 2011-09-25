/*
  *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */
package org.asterope.util
import java.text.DecimalFormat

/**
 * An wrapper for angle. Normally angle would be stored in double
 * as radians, but this introduces rounding errors.
 * This class stores value in microarc seconds to prevent rounding errors.
 * <p>
 * Usage examples:
 * <code>
 *   //import provides implicit conversions for numbers
 *   import org.asterope.util._
 *   //use implicit conversions to construct angle from number
 *   val angle = 10.degree + 0.5.arcSec
 *   //convert value to radian an print it
 *   println(11.toRadian)
 * </code>
 */
@SerialVersionUID(-734158430753489490L)
case class Angle(uas: Long) extends Ordered[Angle]{

  //require(uas> - Angle.CIRCLE && uas < Angle.CIRCLE, "out of range, possible overflow ");

  //operators
  def + (a2:Angle) = new Angle(uas + a2.uas);
  def - (a2:Angle) = new Angle(uas - a2.uas);
  def * (a2:Double) = new Angle((uas * a2).toLong);
  def * (a2:Int) = new Angle(uas * a2);
  def / (a2:Double) = new Angle((uas / a2).toLong);
  def / (a2:Int) = new Angle(uas / a2);

  def unary_+  = this;
  def unary_-  = Angle(-uas);

  override def compare(that: Angle) = uas.compare(that.uas) 

  /** returns angle value in radians*/
  def toRadian = Angle.Uas2R * uas;
  /** returns angle value in degrees*/
  def toDegree = Angle.Uas2D * uas
  /** returns angle value in mili arc seconds*/
  def toMas = uas * 1e-3

  /** returns angle value in arc seconds*/
  def toArcSec:Double =  1e-6 * uas

  /** returns Angle with value normalized between 0 to 2*PI */
  def normalizedRa:Angle = {
	  var uas2 = uas;
      while(uas2<0) uas2 += Angle.CIRCLE
	  uas2 = uas2%Angle.CIRCLE

	  return new Angle(uas2);
  }
  
  override def toString = "Angle("+toDegree+" degree)";

  /** retuns sequence of angles with given max value and increment*/
  def to(maxVal:Angle, increment:Angle):Seq[Angle] = (uas to (maxVal.uas, increment.uas)).map(Angle(_))
  /** retuns sequence of angles with given max value and increment*/
  def until(maxVal:Angle, increment:Angle):Seq[Angle] = (uas until (maxVal.uas, increment.uas)).map(Angle(_))

}




object Angle{	
	
  protected[util] val CIRCLE:Long= 360l * 60l * 60l * 1000l * 1000l;


  /** used in implicit conversion to support `1.degree`, `1.arcMinute` etc */
  protected[util] class AngleWrapperDouble(d:Double){
  	def degree = Angle((d * Angle.D2Uas).toLong);
  	def arcMinute = Angle((d * Angle.M2Uas).toLong);
  	def arcSec = Angle((d * Angle.S2Uas).toLong);
    def arcHour = Angle((d * Angle.H2Uas).toLong);
	  def radian = Angle((d * Angle.R2Uas).toLong);
	  def mas = Angle((d * 1000).toLong);
  }

  //implicit conversions
  protected[util] def long2angle(d:Long) = new AngleWrapperDouble(d);
  protected[util] def int2angle(d:Int) = new AngleWrapperDouble(d);
  protected[util] def double2angle(d:Double) = new AngleWrapperDouble(d);
  
  //def radian2angle(rad: Double) = new Angle((Angle.R2Uas * rad).toLong)
  //def degree2angle(deg: Double) = new Angle((Angle.D2Uas * deg).toLong)
 
  /** returns random angle with value between 0 and 2*PI */
  def randomRa():Angle = new Angle((CIRCLE * math.random).asInstanceOf[Int]);
  /** returns random angle with value between -PI/2 and + PI/2 */
  def randomDe():Angle = new Angle((CIRCLE/2 * math.random - CIRCLE/4).asInstanceOf[Int]);

  /** returns maximal angle from two options */
  def max(a1:Angle, a2:Angle) =  if(a1>a2) a1 else a2
  /** returns minimal angle from two options */
  def min(a1:Angle, a2:Angle) =  if(a1<a2) a1 else a2

    /**
     * Parse Declination from four values. It uses BigDecimal, so there are no rounding errors
     *
     * @param deSign   signum (ie + or -)
     * @param deDegree declination in degrees
     * @param deMin    remaining part in arc minutes
     * @param deSec    remaining part in arc seconds
     * @return declination in Micro Arc Seconds */
    def parseDe(deSign: String, deDegree: String, deMin: String, deSec: String): Angle = {
      val sign: Int = if ("-".equals(deSign.trim)) -1 else 1
      import java.math.BigDecimal
      val deg: BigDecimal = new BigDecimal(deDegree)
      if (deg.doubleValue < 0 || deg.doubleValue > 89) throw new IllegalArgumentException("Invalid deDegree: " + deg)
      val min: BigDecimal = if (deMin != null) new BigDecimal(deMin) else BigDecimal.ZERO
      if (min.doubleValue < 0 || min.doubleValue >= 60) throw new IllegalArgumentException("Invalid deMin: " + min)
      val sec: BigDecimal = if (deSec != null) new BigDecimal(deSec) else BigDecimal.ZERO
      if (sec.doubleValue < 0 || sec.doubleValue >= 60) throw new IllegalArgumentException("Invalid deSec: " + sec)
      return Angle(sign *
        (deg.multiply(new BigDecimal(Angle.D2Uas)).longValueExact +
          min.multiply(new BigDecimal(Angle.M2Uas)).longValueExact +
          sec.multiply(new BigDecimal(Angle.S2Uas)).longValueExact))
    }

  /**
   * Tries to parse Angle from string.
   * It knows common formats used for Declination
   */
    def parseDe(de: String): Angle = {
      if (de == null) throw new IllegalArgumentException("de is null")
      val r1 = ("([+|-]?)([0-9]+)["+Angle.DEGREE_SIGN+"d: ]{1,2}([0-9]+)[m': ]{1,2}([0-9\\.]+)[s\\\"]?").r
      val r2 = ("([+|-]?)([0-9]+)["+Angle.DEGREE_SIGN+"d: ]{1,2}([0-9]+)[m']?").r
      de match {
        case r1(sign,d,m,s) =>
          parseDe(sign,d,m,s)
        case r2(sign,d,m) =>
          parseDe(sign,d,m,null)
        case _ => throw new IllegalArgumentException("Could not parse DE: " + de)
      }
    }

  /**
     * parse Right ascencion  from triple values raHour raMin, raSec
     * This method uses big decimal, so there are no rounding errors
     *
     * @param raHour
     * @param raMin
     * @param raSec
     * @return result in micro arc seconds */
    def parseRa(raHour: String, raMin: String, raSec: String): Angle = {
      import java.math.BigDecimal
      val raHour2: BigDecimal = new BigDecimal(raHour)
      if (raHour2.doubleValue < 0 || raHour2.doubleValue > 23) throw new IllegalArgumentException("Invalid raHour: " + raHour2)
      val raMin2: BigDecimal = if (raMin != null) new BigDecimal(raMin) else BigDecimal.ZERO
      if (raMin2.doubleValue < 0 || raMin2.doubleValue >= 60) throw new IllegalArgumentException("Invalid raMin: " + raMin2)
      val raSec2: BigDecimal = if (raSec != null) new BigDecimal(raSec) else BigDecimal.ZERO
      if (raSec2.doubleValue < 0 || raSec2.doubleValue >= 60) throw new IllegalArgumentException("Invalid raSec: " + raSec2)
      return Angle(
        raHour2.multiply(new BigDecimal(Angle.H2Uas)).longValueExact +
        raMin2.multiply(new BigDecimal(Angle.HMin2Uas)).longValueExact +
        raSec2.multiply(new BigDecimal(Angle.HSec2Uas)).longValueExact
      )
    }
    /**
      * Tries to parse Angle from string.
      * It knows common formats used for Right ascencion (including hours)
      */
    def parseRa(ra: String): Angle = {
      if (ra == null) throw new IllegalArgumentException("ra is null")
      val r1 = "([0-9]+)[h: ]{1,2}([0-9]+)[m: ]{1,2}([0-9\\.]+)[s]{0,1}".r
      val r2 = "([0-9]+)[h: ]{1,2}([0-9\\.]+)[m]?".r
      val r3 = "([0-9]+)d([0-9]+)m([0-9\\.]+)s".r
      ra match {
        case r1(h,m,s) => parseRa(h,m,s)
        case r2(h,m) => parseRa(h,m,null)
        case r3(d,m,s) => d.toDouble.degree + m.toDouble.arcMinute + m.toDouble.arcSec
        case _ => throw new IllegalArgumentException("Could not parse RA: " + ra)
      }
    }

    /**
   * Parses pair of RA and De coordinates.
   * This method should handle formats used in vizier.
   * An example:
   * The following writings are allowed:
   * <pre>
      20 54 05.689 +37 01 17.38
      10:12:45.3-45:17:50
      15h17m-11d10m
      15h17+89d15
      275d11m15.6954s+17d59m59.876s
      12.34567h-17.87654d
      350.123456d-17.33333d <=> 350.123456 -17.33333
    </pre>
   */
  def parseRaDe(str:String):(Angle,Angle) = {
    //20 54 05.689 +37 01 17.38
    //10:12:45.3-45:17:50
    lazy val r1 = "([0-9]{2})[ :]([0-9]{2})[ :]([0-9]{2}\\.[0-9]+)[ ]?(\\+|-)([0-9]{2})[ :]([0-9]{2})[ :]([0-9]{2}\\.?[0-9]*)".r
    //15h17m-11d10m
    //15h17+89d15
    lazy val r2 = "([0-9]{2})h([0-9]{2})[m]?(\\+|-)([0-9]{2})d([0-9]{2})[m]?".r
    //275d11m15.6954s+17d59m59.876s
    lazy val r3 = "([0-9]{2,3}d[0-9]{2}m[0-9]{2}\\.[0-9]+s)([\\+-][0-9]{2}d[0-9]{2}m[0-9]{2}\\.[0-9]+s)".r
    //12.34567h-17.87654d
    lazy val r4 = "([0-9]{1,2}\\.[0-9]+)h([\\+-][0-9]{2}\\.[0-9]+)d".r
    //350.123456d-17.33333d <=> 350.123456 -17.33333
    lazy val r5 = "([0-9]{1,3}\\.?[0-9]*)[d]?[ ]?([\\+-]?[0-9]{1,2}\\.?[0-9]*)[d]?".r

    str match {
      case r1(ah,am,as,ss,d,m,s) => (parseRa(ah,am,as), parseDe(ss,d,m,s))
      case r2(ah,am,ss,d,m) => (parseRa(ah,am,null), parseDe(ss,d,m,null))
      case r3(ra,de) => (parseRa(ra), parseDe(de))
      case r4(ra,de) => (ra.toDouble.arcHour, de.toDouble.degree)
      case r5(ra,de) => (ra.toDouble.degree, de.toDouble.degree)

    }

  }
  
  
  
    /**
     * normalize RA into 0 - 2 * PI range */
    def normalizeRa(ra2:Double):Double ={
      var ra = ra2
        while (ra < 0)
            ra += math.Pi * 2;
        while (ra >= math.Pi * 2)
            ra -= math.Pi * 2;

        return ra;
    }

    def assertRa(ra:Double) {
        if (ra < 0 || ra >= math.Pi * 2d)
            throw new IllegalArgumentException("Invalid RA: " + ra);
    }

    def assertDe(de:Double) {
        if (de < -Angle.D2R * 90d || de > Angle.D2R * 90d)
            throw new IllegalArgumentException("Invalid DE: " + de);
    }
    
    
    private def isNear(x:Double, d:Double):Boolean = {
        val tolerance = 1e-7;
        math.abs(x%d)<tolerance || math.abs(x%d -d)<tolerance;
    }
  
    private val oneZeroFormat = new DecimalFormat("##.#")
    private val twoZeroFormat = new DecimalFormat("##.##")
    
    /**
     * convert RA to string in format '1h 2m'
     * minutes and seconds are auto added as needed
     *
     * @param ra in radians
     * @return ra in string form  */
    def raToString(ra:Double):String ={        
        if(isNear(ra, H2R)){
            val hour = math.round(ra*R2H).toInt;
            return ""+hour + "h";
        }else if(isNear(ra, H2R/60)){
            val hour = (ra*R2H).toInt;
            val min = Math.round((ra - H2R * hour)*R2H*60).toInt;
            return ""+hour+"h "+min+"m";
        }else{
            val hour =  (ra*R2H).toInt
            val min = ((ra - H2R * hour)*R2H*60).toInt
            val sec = ((ra - H2R * hour - min * H2R/60)*R2H*3600);
            var s = "";
            if(isNear(sec, 1))
                s = ""+math.round(sec);
            else if(isNear(sec,0.1))
                s = oneZeroFormat.format(sec)
            else if(isNear(sec,0.01))
                s = twoZeroFormat.format(sec)
            else
                s = ""+sec;

            return ""+hour+"h "+min+"m "+s+"s";
        }
    }

    /**
     * convert DE to string in format '1d 2m'
     * minutes and seconds are auto added as needed
     *
     * @param de2 in radians
     * @return de in string form */
    def deToString(de2:Double):String = {
        var de = de2;
        var sign = "";
        if(de<0){
            de = - de;
            sign = "-";
        }

        if(isNear(de, D2R)){
            val deg = math.round(de*R2D).toInt;
            return sign+deg + DEGREE_SIGN;
        }else if(isNear(de, M2R)){
            val deg = (de*R2D).toInt;
            val min = ((de - D2R * deg)*R2M).toInt;
            return sign+deg+DEGREE_SIGN+min+"'";
        }else{
            val deg = (de*R2D).toInt;
            val min = ((de - D2R * deg)*R2D*60).toInt;
            val sec = ((de - D2R * deg - min * D2R/60)*R2D*3600);
            var s = "";
            if(isNear(sec, 1))
                s = ""+math.round(sec);
            else if(isNear(sec,0.1))
                s = oneZeroFormat.format(sec)
            else if(isNear(sec,0.01))
                s = twoZeroFormat.format(sec)
            else
                s = ""+sec;

  
            return sign+deg+DEGREE_SIGN+min+"'"+s+"\"";
        }

    }
  
    /**
     * calculate great circle distance of two points,
     * coordinates are given in radians
     *
     * @return distance of two points in radians */
    def distance(ra1:Double,de1:Double,ra2:Double,de2:Double):Double = {
        //check ranges
        assertRa(ra1);
        assertRa(ra2);
        assertDe(de1);
        assertDe(de2);

        //this code is from JH labs projection lib
        val dlat = math.sin((de2 - de1) / 2);
        val dlon = math.sin((ra2 - ra1) / 2);
        val r = math.sqrt(dlat * dlat + Math.cos(de1) * Math.cos(de2) * dlon * dlon);
        2.0 * math.asin(r);        
    }
    
    
    
    /**multiply to convert degrees to radians */
    val D2R = math.Pi / 180d;

    /**multiply to convert radians to degrees*/
    val R2D = 1d / D2R;


    /**multiply to convert degrees to arc hours*/
    val D2H = 1d / 15d;

    /**multiply to convert arc hour to degrees */
    val H2D = 1d / D2H;

    /**  multiply to convert degrees to arc minute */
    val D2M = 60;

    /** multiply to convert arc minute  to toDegree */
    val M2D = 1d / D2M;

    /** multiply to convert degrees to arc second */
    val D2S = 3600;

    /** multiply to convert arc second to toDegree */
    val S2D = 1d / D2S;

    /** multiply to convert hours to radians */
    val H2R = Angle.H2D * Angle.D2R;

    /** multiply to convert radians to hours */
    val R2H = 1d / H2R;

    /** multiply to convert radians to minutes */
    val R2M = R2D * D2M;

    /** multiply to convert minutes to radians */
    val M2R = 1d / R2M;

    /** multiply to convert mili arc seconds to radians */
    val Mas2R = D2R / 3600000d;
    /** multiply to convert micro arc seconds to radians */
    val Uas2R = D2R / 3600000000d;


    /** multiply to convert radians to mili arc seconds */
    val R2Mas = 1d / Mas2R;

    /** multiply to convert radians to micro arc seconds */
    val R2Uas = 1d / Uas2R;


    /** multiply to convert hours to mili arc seconds */
    val H2Mas = 15 * 60 * 60 * 1000;

    /** multiply to convert time minutes to mili arc seconds */
    val HMin2Mas = 15 *  60 * 1000;

    /** multiply to convert time seconds to mili arc seconds */
    val HSec2Mas = 15 * 1000;

    /** multiply to convert hours to micro arc seconds */
    val H2Uas = 15l * 60l * 60l * 1000l * 1000l;

    /** multiply to convert time minutes to micro arc seconds */
    val HMin2Uas = 15l *  60l * 1000l * 1000l;

    /** multiply to convert time seconds to micro arc seconds */
    val HSec2Uas = 15l * 1000l * 1000l;



    /** multiply to convert degrees to mili arc seconds */
    val D2Mas =  60 * 60 * 1000;

    /** multiply to convert minutes to mili arc seconds */
    val M2Mas =  60 * 1000;

    /** multiply to convert Seconds to mili arc seconds */
    val S2Mas =  1000;


    /** multiply to convert degrees to micro arc seconds */
    val D2Uas=  60l * 60l * 1000l * 1000l;

    /** multiply to convert minutes to micro arc seconds */
    val M2Uas =  60l * 1000l * 1000l;

    /** multiply to convert Seconds to micro arc seconds */
    val  S2Uas =  1000l * 1000l;


	/** multiply to convert UAS to degrees  */
    val Uas2D =  1d/D2Uas;

    /** multiply to convert UAS to minutes  */
    val Uas2M=  1d/M2Uas;

    /** multiply to convert UAS to Seconds  */
    val Uas2S =  1d/S2Uas;


    /** multiply to convert  arc seconds to radians */
    val S2R = D2R / 3600d;

    /** multiply to convert radians to  arc seconds */
    val R2S = 1d / S2R;

    /** round circle which marks degrees */
    val DEGREE_SIGN = '\u00B0';
  

}