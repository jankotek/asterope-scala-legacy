package org.asterope.gui

import org.asterope.util._
import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import java.awt.event._

import edu.umd.cs.piccolo.util.PBounds
import nodes.PPath
import org.asterope.chart._
import java.util.concurrent._
import collection.mutable.ArrayBuffer
import java.awt.{Rectangle, Color}
import java.awt.geom.{Ellipse2D, Area}
import org.apache.commons.math.geometry.Vector3D
;


class ChartEditor(
    val beans:ChartBeans
  )
  extends PCanvas
  with ChartWindowSkyviewActions
  with ChartEditorActions
  {

  setPanEventHandler(null)
  setZoomEventHandler(null)
  setBackground(java.awt.Color.black)


    //install zoom handler
    getCamera.addInputEventListener(new PBasicInputEventHandler {
      override def mouseClicked(event: PInputEvent) {
        //change selection with mid button
        if(event.isMouseEvent && event.getClickCount == 1 && event.getButton == 1){
          event.setHandled(true)
          val node = event.getPickedNode
          val obj = if(node==null) None else chartBase.getObjectForNode(node)
          selectObject(obj)
        } else if (event.isMouseEvent && event.getClickCount == 1 && event.getButton == 2) {
          //center on new position with mid mouse button
          event.setHandled(true);
          if(!getInteracting) setInteracting(true)

          // if middle button is pressed, center at given location
          val viewPos = event.getPosition;
          val width = chartBase.camera.getViewBounds.getWidth;
          val height = chartBase.camera.getViewBounds.getHeight;
          val bounds = new PBounds(viewPos.getX - width / 2, viewPos.getY - height / 2, width, height);
          chartBase.camera.setViewBounds(bounds);
          refresh()
       }
      }
      override def mouseWheelRotated(event: PInputEvent) {
        //zoom with mouse wheel
        if (event.isMouseWheelEvent) {
          event.setHandled(true);
          if(!getInteracting) setInteracting(true)

          // handles zoom event on mouse wheel
          val newScale = 1 + 0.1 * event.getWheelRotation;
          val viewPos = event.getPosition;
          chartBase.camera.scaleViewAboutPoint(newScale, viewPos.getX, viewPos.getY);
          refresh()

        }
      }
    })




  //refresh when canvas size changes
  addComponentListener(new ComponentAdapter{
    override def componentResized(e:ComponentEvent){
      refresh()
    }
  })
  
  /**
   * Notify when chart refresh starts 
   */
  lazy val onChartRefreshStart = new Publisher[Chart]()

  /**
   * Notify when chart refresh finishes 
   */
  lazy val onChartRefreshFinish = new Publisher[Chart]()


  protected var chartBase = new Chart(executor = new EDTChartExecutor);
  protected var coordGridConfig = CoordinateGrid.defaultConfig
  protected var starsConfig = beans.stars.defaultConfig
  protected var showLegend = true
  protected var showConstelBounds = true
  protected var showConstelLines = true;
  protected var deepSkyConfig = beans.deepSky.defaultConfig
  protected var allSkyConfig:Option[AllSkySurveyMem] = None


  def getChartBase = chartBase

  private var refreshWorker:Future[Chart] = null

  def refreshInProgress:Boolean = onEDTWait{
    refreshWorker == null || !refreshWorker.isDone
  }

  def refresh(){
    //cancel previously running tasks
    if(refreshWorker!=null ){
      refreshWorker.cancel(true);
    }

    refreshWorker = future[Chart]{
      if (getInteracting){
        //wait a bit in case user is zooming or performing other interactive task
        //this way new tasks may cancel this future without even starting
        Thread.sleep(500);

        //if user used mouse to move chart, center on new position and update FOV
        val bounds = chartBase.camera.getViewBounds;
        val center = chartBase.wcs.deproject(bounds.getCenter2D);
        if(center.isDefined){
          val fov = chartBase.wcs.deproject(bounds.getOrigin).map(Vector3D.angle(_,center.get) * 2).getOrElse(120 * Angle.D2R);
          chartBase = chartBase.copy(position = center.get, fieldOfView = fov.radian)
        }
      }

      //take current size of windows
      val chart = chartBase.copy(width = getWidth,
        height = if( !showLegend) getHeight else (getHeight - beans.legendBorder.height),
        legendHeight = if(showLegend) beans.legendBorder.height else 0,
        executor = new EDTChartExecutor
      )

      onChartRefreshStart.firePublish(chart)

      val futures = new ArrayBuffer[Future[Unit]];

      futures+=future{
        beans.stars.updateChart(chart,starsConfig)
      }
      futures+=future{
        beans.deepSky.updateChart(chart,deepSkyConfig)
      }

      if(showConstelBounds) futures+=future{
          beans.constelBoundary.updateChart(chart)
      }

      if(showConstelLines) futures+=future{
          beans.constelLine.updateChart(chart)
      }


      futures+=future{
        beans.milkyWay.updateChart(chart)
      }


      futures+=future{
        CoordinateGrid.updateChart(chart,coordGridConfig)
      }

      if(showLegend) futures+=future{
          beans.legendBorder.updateChart(chart)
      }

      futures+=future{
        overview.update(chart)
      }


      //now wait for all futures to finish
      waitOrInterrupt(futures)

      //good now perform final tasks on EDT
      onEDTWait{
        //labels must be last,
        // placement algorithm depends on graphic created by other features
        Labels.updateChart(chart)
        chartBase = chart;
        chartBase.executor.asInstanceOf[EDTChartExecutor].plugIntoSwing()
        getCamera.removeAllChildren();
        if(getInteracting)
           setInteracting(false) //this will cause repaint, but chart is already empty so no performance problem

        //restore selection
        selectObject(selectedObject,selectionAfterRefresh=true)

        getCamera.addChild(chartBase.camera)
        onChartRefreshFinish.firePublish(chartBase)
      }

      allSkyConfig.foreach{mem=>
//        futures+=future{
          AllSkySurvey.updateChart(chart,mem)
//        }
      }

      chart
    }

  }


  def centerOnPosition(pos:Vector3D){
    chartBase = chartBase.copy(position = pos)
    refresh()
  }


  private var selectedPointer:Option[PNode] = None
  private var selectedObject:Option[Any] = None
  def getSelectedObject = selectedObject


  val onSelectionChanged = new Publisher[Chart]()

  def selectObject(obj:Option[Any], selectionAfterRefresh:Boolean = false){

    def createPointer(node:PNode):PNode = {
      val w = 18
      val h = 2
      val d = math.max(w+10,node.getFullBoundsReference.width.toInt/2 + 10)

      val area = new Area(new Ellipse2D.Double(-d-h, -d-h, (d+h)*2, (d+h)*2));
      area.subtract(new Area(new Ellipse2D.Double(-d, -d, d*2, d*2)))
      area.add(new Area(new Rectangle(-d-w,-h/2,w*2,h)))
      area.add(new Area(new Rectangle(d-w,-h/2,w*2,h)))
      area.add(new Area(new Rectangle(-h/2,-d-w,h,w*2)))
      area.add(new Area(new Rectangle(-h/2,d-w,h,w*2)))

      val n = new PPath(area)
      n.setPaint(Color.red)
      n.centerFullBoundsOnPoint(node.getFullBoundsReference.getCenterX, node.getFullBoundsReference.getCenterY)
      n;
    }

    if(obj == selectedObject && !selectionAfterRefresh){
      return
    }

    //unselect old
    selectedPointer.foreach(_.removeFromParent())
    selectedPointer = None;

    obj.foreach{o=>
      val node:PNode = chartBase.getNodeForObject(o).getOrElse(throw new IllegalArgumentException("Object not on map"))
      val pointer = createPointer(node);
      selectedPointer = Some(pointer)
      chartBase.addNode(Layer.fg, pointer, async=false)
      pointer.repaint()
    }

    if(selectedObject!=obj){
      selectedObject = obj
      onSelectionChanged.firePublish(chartBase)
    }


  }


  val overview = new PCanvas{
    private var _chart:Chart = new Chart();
    def chart = _chart;
    setPanEventHandler(null)
    setZoomEventHandler(null)
    setBackground(java.awt.Color.black)

    def update(detailChart:Chart){
      _chart = _chart.copy(
        position=detailChart.position,
        fieldOfView = detailChart.fieldOfView * 4,
        width = getWidth,
        height =getHeight,
        colors = detailChart.colors
      )
      beans.stars.updateChart(_chart)
      beans.constelBoundary.updateChart(_chart)
      beans.constelLine.updateChart(_chart)
      onEDTWait{
        getCamera.removeAllChildren()
        getCamera.addChild(_chart.camera)
      }
    }
  }


}