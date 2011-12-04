package org.asterope.gui

import org.asterope.Beans

/**
 * Trait mixed in into MainView.
 * It provides Actions for MainView which are forwarded to active ChartEditor
 * It also provides factory method to create new ChartEditor tab
 */
trait ChartEditorFab extends  MainWindow{

  val beans:Beans

  def openChartOnObject(objName:String){
    val pos = beans.nameResolver.resolve(objName).pos.get
    val comp = new ChartEditor(beans)
    addEditor(objName,comp)
    comp.centerOnPosition(pos)

  }



}
