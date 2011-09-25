package org.asterope.gui

import javax.swing._


import collection.mutable.ListBuffer
import junit.framework.AssertionFailedError
import java.util.concurrent.{TimeUnit, CountDownLatch}
import org.asterope.util._
import java.awt.{Window, Component, Container}

/**
 * Test case which helps testing GUI using FEST. 
 * <p>
 * It opens window and allows performing some operations
 * <p> 
 * @see org.asterope.context.Context.TestCase
 *
 */
abstract class GuiTestCase extends ScalaTestCase{


//  val guiBeans = onEDTWait{
//    new GuiBeans{}
//  }

  private var chartWinInitialized = false

//  lazy val chartWin = onEDTWait{
//    val v = guiBeans.chartWindow
//    chartWinInitialized = true
//    v.setState(java.awt.Frame.ICONIFIED)
//    v.setVisible(true)
//    v
//  }

  override def setUp{
    CheckThreadViolationRepaintManager.hook();
  }

//  override def tearDown{
//    try{
//      if(chartWinInitialized){
//        onEDTWait{chartWin.setVisible(false)}
//        chartWin.dispose
//      }
//      guiBeans.shutdown()
//    }finally{
//      super.tearDown() //make sure previous context is disposed
//    }
//  }

  def sleep(i:Int){
    assertNotEDT()
    Thread.sleep(i)
    //now need to wait until EDT queue is empty
    //easiest way is to send an event, and wait until itis executed
    val latch = new CountDownLatch(1)
    onEDT{latch.countDown}
    latch.await(10000,TimeUnit.MILLISECONDS)
    assert(latch.getCount == 0,"EDT dead block")
  }

  def sleep(){
    sleep(500)
  }


//  /** pause current thread until chart is refreshed */
//  def waitUntilChartRefreshEnd(){
//    val lock = new Object();
//    chartWin.onChartRefreshFinish{m=>
//      lock.synchronized{lock.notifyAll}
//    }
//    //sleep until lock is notified
//    lock.synchronized{lock.wait}
//  }


  /**
   * wait for 30 seconds, until conditions became true,
   * otherwise it fails
   */
  def waitUntil(msg:String,b: =>Boolean){
    var interval = 100
    var counter = 30000
    while(!b){
      counter -= interval
      sleep(interval)
      if(counter<0)
        fail(msg)
    }
  }

  /** wait until new window is shown */
  def waitForWindow(){
    val windowCounts =  Window.getWindows.size
    waitUntil("Wait for new Window",{
      windowCounts != Window.getWindows.size
    })
  }

  /**
   * @return list of all children and grand children...
   */
  def allSiblings(component:Component):List[Component] = {
    onEDTWait{
      val ret = new ListBuffer[Component]
      //recursive function which traverses hierarchy and adds all child to ListBuffer
      def recur(c:Component){
        ret+=c
        if(c.isInstanceOf[Container])
          c.asInstanceOf[Container].getComponents.foreach(recur)
      }
      //trigger that thing

      recur(component)
      ret.toList
    }
  }

  /**
   * Find Button with given name
   */
  def findButton(container:Container,name:String):AbstractButton = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[AbstractButton])
      .map(_.asInstanceOf[AbstractButton])
      .getOrElse(throw new AssertionFailedError("AbstractButton with name '"+name+"' was not found"))
  }


  /**
   * Find JTextField with given name
   */
  def findJTextField(container:Container,name:String):JTextField = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JTextField])
      .map(_.asInstanceOf[JTextField])
      .getOrElse(throw new AssertionFailedError("JTextField with name '"+name+"' was not found"))
  }


  /**
   * Find JTextArea with given name
   */
  def findJTextArea(container:Container,name:String):JTextArea = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JTextArea])
      .map(_.asInstanceOf[JTextArea])
      .getOrElse(throw new AssertionFailedError("JTextArea with name '"+name+"' was not found"))
  }


  /**
   * Find JList with given name
   */
  def findJList(container:Container,name:String):JList[Any] = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JList[Any]])
      .map(_.asInstanceOf[JList[Any]])
      .getOrElse(throw new AssertionFailedError("JList with name '"+name+"' was not found"))
  }

  /**
   * Find JLabel with given name
   */
  def findJLabel(container:Container,name:String):JLabel = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JLabel])
      .map(_.asInstanceOf[JLabel])
      .getOrElse(throw new AssertionFailedError("JLabel with name '"+name+"' was not found"))
  }

  /**
   * Find JCheckBox with given name
   */
  def findJCheckBox(container:Container,name:String):JCheckBox = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JCheckBox])
      .map(_.asInstanceOf[JCheckBox])
      .getOrElse(throw new AssertionFailedError("JCheckBox with name '"+name+"' was not found"))
  }

  /**
   * Find JSlider with given name
   */
  def findJSlider(container:Container,name:String):JSlider = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JSlider])
      .map(_.asInstanceOf[JSlider])
      .getOrElse(throw new AssertionFailedError("JSlider with name '"+name+"' was not found"))
  }

  /**
   * Find JComboBox with given name
   */
  def findJComboBox(container:Container,name:String):JComboBox[Any] = {
    allSiblings(container)
      .find(_.getName == name)
      .filter(_.isInstanceOf[JComboBox[Any]])
      .map(_.asInstanceOf[JComboBox[Any]])
      .getOrElse(throw new AssertionFailedError("JComboBox with name '"+name+"' was not found"))
  }


  /**
   * Check or uncheck an button, setSelected does not trigger listeners
   */
  def setChecked(button:AbstractButton, checked:Boolean){
    if(button.isSelected() != checked)
      button.doClick
  }

}