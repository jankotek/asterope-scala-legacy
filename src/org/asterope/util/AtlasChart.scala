package org.asterope.util

import org.apache.commons.math.geometry.Vector3D

/**
 * A class to obtain the chart that best shows a given position in different
 * atlases.
 * <P>
 * It also supports the Rukl lunar atlas given a lunar latitude and longitude.
 *
 * @author Mark Huss
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
object AtlasChart{
  /**
     * This function returns the page number in the Millennium Star Atlas that
     * best shows the location specified.
     *
     * @param ra Right ascension in radians.
     * @param dec Declination in radians.
     * @return The appropriate Millenium Atlas page.
     */
    def millenniumAtlas(ra2: Double, dec2: Double): Int = {
      var page: Int = 0
      val ra = ra2 * Angle.R2H
      val dec = dec2 * Angle.R2D
      if (dec >= 87.)
        page = (if (ra < 4|| ra > 16) 2 else 1)
      else if (dec <= -87.)
        page = (if (ra < 4|| ra > 16) 516 else 515)
      else {
        val gore: Int = (ra / 8.).asInstanceOf[Int]
        var zone: Int = ((93.- dec) / 6.).asInstanceOf[Int]
        val remains: Double = math.ceil(ra / 8.) * 8.- ra
        val per_zone = Array(2, 4, 8, 10, 12, 14, 16, 20, 20, 22, 22, 24, 24, 24, 24, 24, 24, 24, 24, 24, 22, 22, 20, 20, 16, 14, 12, 10, 8, 4, 2)
        page = (remains * per_zone(zone).asInstanceOf[Double] / 8.).asInstanceOf[Int] + 1 + gore * 516
        while (0 != ({
          zone -= 1; zone
        })) page += per_zone(zone)
      }
      return page
    }

  /**
     * This function returns the page number in Sky Atlas 2000 page that best
     * shows the location specified.
     *
     * @param ra Right ascension in radians.
     * @param dec Declination in radians.
     * @return The appropriate Sky Atlas 2000 page.
     */
    def skyAtlas2000(ra2: Double, dec2: Double): Int = {
      var page: Int = 0
      val ra = ra2 * Angle.R2H
      val dec = dec2 * Angle.R2D
      if (math.abs(dec) < 18.5) {
        page = 9 + (ra / 3.0+ 5.0/ 6.0).toInt
        if (page == 9) page = 17
      }
      else if (math.abs(dec) < 52.0) {
        page = 4 + (ra / 4.0).toInt
        if (dec < 0.) page += 14
      }
      else {
        page = 1 + (ra / 8.).toInt
        if (dec < 0.) page += 23
      }
      return page
    }

    private val uranometriaDecLimits = Array(-900, -845, -725, -610, -500, -390, -280, -170, -55, 55, 170, 280, 390, 500, 610, 725, 845, 900)
    private val uranometriaNDivides = Array(2, 12, 20, 24, 30, 36, 45, 45, 45, 45, 45, 36, 30, 24, 20, 12, 2)
    /**
     * This function returns the page number in Uranometria that best shows the
     * location specified.
     *
     * @param ra Right ascension in radians.
     * @param dec Declination in radians.
     * @param fix472 True to swap charts 472 and 473 (needed in original
     *        edition).
     * @return The appropriate Uranometria page.
     */
    def uranometria(ra2: Double, dec2: Double, fix472: Boolean): Int = {
      val ra = ra2 * Angle.R2H
      val dec = dec2 * Angle.R2D
      var divide: Int = 0
      var startValue: Int = 472

      divide = 0
      while (uranometriaDecLimits(divide + 1).toDouble < dec * 10.0) {
        startValue -= uranometriaNDivides(divide + 1).toInt
        divide += 1; divide
      }
      var angle: Double = ra * uranometriaNDivides(divide).asInstanceOf[Int] / 24.0
      if (uranometriaNDivides(divide) >= 20) angle += 0.5
      else if (uranometriaNDivides(divide) == 12) angle += 5.0/ 12.0
      var page: Int = angle.asInstanceOf[Int] % uranometriaNDivides(divide) + startValue
      if (page >= 472 && fix472) page = (472 + 473) - page
      return page
    }

    /**
     * This function returns the page number in the original edition of
     * Uranometria that best shows the location specified.
     *
     * @param ra Right ascension in radians.
     * @param dec Declination in radians.
     * @return The appropriate Uranometria page.
     */
    def uranometria(ra: Double, dec: Double): Int = {
      return uranometria(ra, dec, false)
    }

    def uranometria(pos:Vector3D): Int = uranometria(pos.getRaRadian,pos.getDeRadian)


  /**
     * This function returns the page number in Rukl that best shows the lunar
     * location specified. Returns a String to accomodate Rukl's roman numeral
     * libration pages.
     *
     * @param lon lunar longitude in radians.
     * @param lat lunar latitude in radians.
     * @return The appropriate Rukl page.
     */
    def rukl(lon: Double, lat: Double): String = {
      val x: Double = math.cos(lat) * math.cos(lon)
      val y: Double = math.cos(lat) * math.sin(lon)
      val z: Double = math.sin(lat)
      var page: Int = -1
      var ix: Int = (y * 5.5 + 5.5).toInt
      var iy: Int = (4.0- 4.0* z).toInt
      val page_starts = Array(-1, 7, 17, 28, 39, 50, 60, 68)
      val buff: StringBuffer = new StringBuffer("Rukl ")
      if (x > 0.) {
        if (iy <= 1 || iy >= 6) {
          if (0 == ix) ix = 1
          else if (10 == ix) ix = 9
        }
        if (0 == iy || 7 == iy) if (1 == ix) ix = 2
        else if (9 == ix) ix = 8
        page = ix + page_starts(iy)
        buff.append(page)
      }
      if (x < math.Pi * 8.0/ 180.0&& x > -math.Pi * 8.0/ 180.0) {
        val zone_no: Int = (math.atan2(z, y) * 4.0/ math.Pi + 4.0).toInt
        val librationZonePages= Array("VII", "VI", "V", "IV", "III", "II", "I", "VIII")
        if (page > -1) buff.append('/')
        buff.append(librationZonePages(zone_no))
      }
      return if (-1 == page) "" else buff.toString
    }
  }