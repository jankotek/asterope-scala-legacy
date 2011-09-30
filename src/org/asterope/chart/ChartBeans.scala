package org.asterope.chart

import org.asterope.data.DataBeans

trait ChartBeans extends DataBeans{
  lazy val stars = new ChartStars(liteStarDao)

  lazy val deepSky = new ChartDeepSky(deepSkyDao)
  lazy val milkyWay = new ChartMilkyWay(milkyWayDao)
  lazy val constelLine = new ChartConstelLine(constelLineDao)
  lazy val constelBoundary = new ChartConstelBoundary(constelBoundaryDao)
  lazy val legendBorder = new ChartLegendBorder(stars,deepSky)
}
