package org.asterope.chart

import edu.umd.cs.piccolo.nodes.{PText, PImage}
import collection.JavaConversions._
import org.asterope.util._

class ChartSkyviewTest extends ScalaTestCase{

  def testPaint{

    val chart = new ChartBase(fieldOfView = 0.1 degree)
    val mem = new ChartSkyviewMemento();

    ChartSkyview.updateChart(chart,mem);

    val layer = chart.getLayer(Layer.skyview)
    val children = layer.getAllNodes.toList

    assert(children.size === 3)
    //find if there is one image
    assert(children.filter(_.isInstanceOf[PImage]).size === 1)
    //there should also be text with survey credit
    assert(children.filter(_.isInstanceOf[PText]).size === 1)

  }

}