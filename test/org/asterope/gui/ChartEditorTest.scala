package org.asterope.gui

import org.asterope.util._
import javax.swing.JMenuBar
import edu.umd.cs.piccolo.PNode
import org.asterope.chart.{Layer, ChartBeans}
import collection.JavaConversions._
import org.asterope.data._
import org.apache.commons.math.geometry.Vector3D

class ChartEditorTest extends ScalaTestCase
  with MainWindow
  with ChartEditorFab{

  object chartBeans extends ChartBeans with TestRecordManager

  val menu = new JMenuBar

  def chart = chartEditor.getChartBase
  def chartEditor = getFocusedEditor.asInstanceOf[ChartEditor]

  
  def open(){
    onEDT{
      showMinimized()
      openChartOnObject("M45")
    }
    onEDTWait{
      waitUntil(getFocusedEditor.isInstanceOf[ChartEditor])
    }
  }

  override def tearDown(){
    onEDT{hide()}
  }

  def testOpenChartOnObject(){
    open()
    assert(Vector3D.angle(chart.position,Vector3D_asterope) < 1 * Angle.D2R)
  }

  def testResizeWindow(){
    open()
    chartEditor.waitForRefresh()
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
    chartEditor.waitForRefresh()
    assert (chart.fieldOfView < oldFov);
  }

  def testZoomOut(){
    open()
    val oldFov = chart.fieldOfView;
    chartEditor.actZoomOut.call()
    chartEditor.waitForRefresh()
    assert (chart.fieldOfView > oldFov);
  }

  def testChartFov15d(){
    open()
    chartEditor.actFov15d.call()
    chartEditor.waitForRefresh()
    assert(chart.fieldOfView === 15.degree)
  }

  def testChartFov8d(){
    open()
    chartEditor.actFov8d.call()
    chartEditor.waitForRefresh()
    assert(chart.fieldOfView === 8.degree)
  }

  def testMapRefresh(){
    open()
    chartEditor.actRefresh.call()
    chartEditor.waitForRefresh()
    //check if there are some stars on chart
    val stars = chart.objects.filter(_.isInstanceOf[LiteStar])
    assert(stars.size ?> 10)
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
    chartEditor.waitForRefresh()
    val starSize = findBiggestStarNode.getWidth
    chartEditor.actBiggerStars.call()
    chartEditor.waitForRefresh()
    assert(starSize<findBiggestStarNode.getWidth)
  }

  def testSmallerStars(){
    open()
    chartEditor.waitForRefresh()
    val starSize = findBiggestStarNode.getWidth
    chartEditor.actSmallerStars.call()
    chartEditor.waitForRefresh()
    assert(starSize>findBiggestStarNode.getWidth)
  }

  def testLegend(){
    open()
    chartEditor.waitForRefresh()
    def labelCount = chart.getLayer(Layer.label).getChildrenCount
    def legendCount = chart.getLayer(Layer.legend).getChildrenCount

    assert(chartEditor.actShowLegend.selected === Some(true))
    assert(labelCount?>0)
    assert(legendCount?>0)

    chartEditor.actShowLegend.call()
    chartEditor.waitForRefresh()
    assert(chartEditor.actShowLegend.selected === Some(false))
    assert(labelCount?>0)
    assert(legendCount === 0)

    chartEditor.actShowLegend.call()
    chartEditor.waitForRefresh()
    assert(chartEditor.actShowLegend.selected === Some(true))
    assert(labelCount?>0)
    assert(legendCount?>0)
  }


  def test_selection_is_shown(){
    open()
    chartEditor.waitForRefresh()
    val m45 = chart.objects.find{ it=>
      it.isInstanceOf[DeepSky] && it.asInstanceOf[DeepSky].names.find(n=> n.toString == "M 45").isDefined
    }.get

    val asterope = chart.objects.find{ it=>
      it.isInstanceOf[LiteStar] && it.asInstanceOf[LiteStar].names.find(n=> n.toString == "Asterope").isDefined
    }.get


    val numberOfNodes = chart.getLayer(Layer.fg).getChildrenCount

    var listenerCounter = 0

    chartEditor.onSelectionChanged{c=>
      listenerCounter +=1
    }

    onEDTWait{
      chartEditor.selectObject(Some(m45))
    }

    assert(listenerCounter === 1)
    assert(numberOfNodes+1 === chart.getLayer(Layer.fg).getChildrenCount)
    assert(chartEditor.getSelectedObject === Some(m45))

    //refresh should not void selection
    chartEditor.refresh()
    chartEditor.waitForRefresh()
    assert(numberOfNodes+1 === chart.getLayer(Layer.fg).getChildrenCount)
    assert(listenerCounter === 1)

    //selecting new object should remove old selection
    onEDTWait{
      chartEditor.selectObject(Some(asterope))
    }
    assert(listenerCounter === 2)
    assert(numberOfNodes+1 === chart.getLayer(Layer.fg).getChildrenCount)
    assert(chartEditor.getSelectedObject === Some(asterope))

    //remove selection
    onEDTWait{
      chartEditor.selectObject(None)
    }
    assert(listenerCounter === 3)
    assert(numberOfNodes === chart.getLayer(Layer.fg).getChildrenCount)
    assert(chartEditor.getSelectedObject === None)

  }



}