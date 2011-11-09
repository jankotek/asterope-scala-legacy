package org.asterope.chart

import org.asterope.util._
import org.asterope.data.TestRecordManager
import org.apache.commons.math.geometry.Vector3D

class ChartConstelBoundaryTest extends ScalaTestCase
  with ChartBeans  with TestRecordManager{


  def testPaintNorthPole{
    val chart = new Chart(position = Vector3D.PLUS_K, fieldOfView = 5.degree)

    constelBoundary.updateChart(chart)
    assert(chart.getLayer(Layer.constelBoundary).getChildrenCount ?>= 3)
  }

}