package org.asterope.gui

import org.asterope.chart.ChartBeans
import net.infonode.docking.View
import javax.swing._

/**
 * Trait mixed in into MainView.
 * It provides Actions for MainView which are forwarded to active ChartEditor
 * It also provides factory method to create new ChartEditor tab
 */
trait ChartEditorFab extends  MainWindow{

  val chartBeans:ChartBeans

  def openChartOnObject(objName:String){
    val pos = chartBeans.nameResolver.resolve(objName).pos.get
    val comp = new ChartEditor(chartBeans)
    addEditor(objName,comp)
    comp.centerOnPosition(pos)

  }



}
