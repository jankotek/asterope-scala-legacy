package org.asterope.chart

import org.asterope.data.DataBeans

trait ChartBeans extends DataBeans{
  lazy val stars = new Stars(liteStarDao)

  lazy val deepSky = new DeepSkyPainter(deepSkyDao)
  lazy val milkyWay = new ChartMilkyWay(milkyWayDao)
  lazy val constelLine = new ChartConstelLine(constelLineDao)
  lazy val constelBoundary = new ChartConstelBoundary(constelBoundaryDao)
  lazy val legendBorder = new LegendBorder(stars,deepSky)
}
