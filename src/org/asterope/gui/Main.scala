package org.asterope.gui

import javax.swing._
import org.asterope.chart.ChartBeans
import org.asterope.util._

/**
 * Main object which starts Asterope GUI
 */
object Main
  extends MainWindow
  with WelcomeEditor
  with ChartEditorFab
  with MainWindowMenu{


  /** assert that all repaints are in GUI thread*/
  RepaintManager.setCurrentManager( new RepaintManager {
    override def addInvalidComponent(component: JComponent){
      assertEDT()
      super.addInvalidComponent(component)
    }

    override def addDirtyRegion(component: JComponent, x: Int, y: Int, w: Int, h: Int){
      assertEDT()
      super.addDirtyRegion(component, x, y, w, h)
    }
  })


  object chartBeans extends ChartBeans

  lazy val messageView = new MessageView(resourceMap)



  def main(args:Array[String]){
    Log.debug("Asterope GUI is starting")
    onEDTWait{
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
      show()
      addEditor("welcomeEditor",welcomeEditor)
      addLeftTopView("objectsView",new JLabel())
      addBottomBarView("messageView",messageView)
    }
  }







}