package org.asterope.gui

import org.asterope.util._
import javax.swing.JMenuBar
import edu.umd.cs.piccolo.PNode
import org.asterope.chart.{Layer, ChartBeans}
import collection.JavaConversions._
import org.asterope.data._

class ChartEditorTest extends ScalaTestCase
  with MainWindow
  with ChartEditorFab{

  object chartBeans extends ChartBeans with TestRecordManager

  val menu = new JMenuBar

  def chart = chartEditor.getChartBase
  def chartEditor = getFocusedEditor.asInstanceOf[ChartEditor]

  def waitForRefresh(){
    //wait max 1 second for refresh to start
    val maxTime = System.currentTimeMillis()+1000;
    waitUntil(chartEditor.isRefreshInProgress == true || System.currentTimeMillis() >maxTime)
    //now wait until refresh finishes
    waitUntil(chartEditor.isRefreshInProgress == false)
  }

  def open(){
    onEDT{
      showMinimized()
      openChartOnObject("M45")
    }
    waitUntil(getFocusedEditor.isInstanceOf[ChartEditor])
  }

  override def tearDown(){
    hide()
  }

  def testOpenChartOnObject(){
    open()
    assert(chart.position.angle(Vector3d.asterope) < 1 * Angle.D2R)
  }

  def testResizeWindow(){
    open()
    waitForRefresh()
    val width = chart.width
    val height = chart.height

    onEDT{
      mainFrame.setSize(500,500)
    }
    //wait until size changes
    waitUntil(width!=chart.width && height!=chart.height)
  }

  def testZoomIn(){
    open()
    val oldFov = chart.fieldOfView
    chartEditor.actZoomIn.call()
    waitForRefresh()
    assert (chart.fieldOfView < oldFov);
  }

  def testZoomOut(){
    open()
    val oldFov = chart.fieldOfView;
    chartEditor.actZoomOut.call()
    waitForRefresh()
    assert (chart.fieldOfView > oldFov);
  }

  def testChartFov15d(){
    open()
    chartEditor.actFov15d.call()
    waitForRefresh()
    assert(chart.fieldOfView === 15.degree)
  }

  def testChartFov8d(){
    open()
    chartEditor.actFov8d.call()
    waitForRefresh()
    assert(chart.fieldOfView === 8.degree)
  }

  def testMapRefresh(){
    open()
    chartEditor.actRefresh.call()
    waitForRefresh()
    //check if there are some stars on chart
    val stars = chart.objects.filter(_.isInstanceOf[LiteStar])
    assert(stars.size > 10)
  }


  def findBiggestStarNode:PNode = {
    chart.getLayer(Layer.star).getChildrenIterator
      .map(_.asInstanceOf[PNode])
      .toList
      .sortWith(_.getWidth > _.getWidth)
      .head
  }

  def testBiggerStars(){
    open()
    waitForRefresh()
    val starSize = findBiggestStarNode.getWidth
    chartEditor.actBiggerStars.call()
    waitForRefresh()
    assert(starSize<findBiggestStarNode.getWidth)
  }

  def testSmallerStars(){
    open()
    waitForRefresh()
    val starSize = findBiggestStarNode.getWidth
    chartEditor.actSmallerStars.call()
    waitForRefresh()
    assert(starSize>findBiggestStarNode.getWidth)
  }

  def testLegend(){
    open()
    waitForRefresh()
    def labelCount = chart.getLayer(Layer.label).getChildrenCount
    def legendCount = chart.getLayer(Layer.legend).getChildrenCount
    assert(chartEditor.actShowLegend.selected === Some(true))
    assert(labelCount?>0)
    assert(legendCount?>0)

    chartEditor.actShowLegend.call()
    waitForRefresh()
    assert(chartEditor.actShowLegend.selected === Some(false))
    assert(labelCount?>0)
    assert(legendCount === 0)

    chartEditor.actShowLegend.call()
    waitForRefresh()
    assert(chartEditor.actShowLegend.selected === Some(true))
    assert(labelCount?>0)
    assert(legendCount?>0)
  }


  def testShowGalaxy(){
    open();
    waitForRefresh()
    onEDT{
      openChartOnObject("M31")
    }
    sleep(1000)          //TODO remove sleep
    assert(chartEditor.actShowGalaxy.selected == Some(true))
    def galaxyFound = chart.objects.find{f=>
      f.isInstanceOf[DeepSky] && f.asInstanceOf[DeepSky].deepSkyType == DeepSkyType.GALXY
    }.isDefined
    assert(galaxyFound)
    chartEditor.actShowGalaxy.call()
    waitForRefresh()
    assert(!galaxyFound)
    assert(chartEditor.actShowGalaxy.selected == Some(false))
  }

  def testShowGlobularCluster(){
    open();
    waitForRefresh()
    onEDT{
      openChartOnObject("M13")
    }
    sleep(1000) //TODO remove sleep
    assert(chartEditor.actShowGlobularCluster.selected == Some(true))
    def clusterFound = chart.objects.find{f=>
      f.isInstanceOf[DeepSky] && f.asInstanceOf[DeepSky].deepSkyType == DeepSkyType.GLOCL
    }.isDefined
    assert(clusterFound)
    chartEditor.actShowGlobularCluster.call()
    waitForRefresh()
    assert(!clusterFound)
    assert(chartEditor.actShowGlobularCluster.selected == Some(false))
  }


}