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

  CheckThreadViolationRepaintManager.hook()
  UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)

  object chartBeans extends ChartBeans

  lazy val messageView = new MessageView(resourceMap)


  def main(args:Array[String]){
    onEDTWait{
      show()
      addEditor("welcomeEditor",welcomeEditor)
      addLeftTopView("objectsView",new JLabel())
      addBottomBarView("messageView",messageView)
    }
  }







}