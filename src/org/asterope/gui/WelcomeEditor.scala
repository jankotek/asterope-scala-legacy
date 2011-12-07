package org.asterope.gui

import javax.swing._
import org.xhtmlrenderer.simple.XHTMLPanel
import org.xhtmlrenderer.swing.LinkListener
import org.xhtmlrenderer.swing.BasicPanel
import scala.collection.JavaConversions._
import org.xhtmlrenderer.swing.FSMouseListener

/**
 * An welcome page displayed first time user starts browser.
 */
class WelcomeEditor(mainWinActions:MainWindowActions){

  lazy val panel = new JScrollPane(htmlPane)

  private object linkHandler extends LinkListener{
    
    override def linkClicked(panel:BasicPanel, link:String){
      if(link.startsWith("asterope:open/"))
        mainWinActions.openChartOnObject(link.replace("asterope:open/",""))
    }
  }


  private lazy val welcomeURL = getClass.getClassLoader.getResource("org/asterope/gui/resources/welcome.html")
  
  System.setProperty("xr.text.aa-fontsize-threshhold","8");

  
  private object htmlPane extends XHTMLPanel{
    
    //install our own listener
    getMouseTrackingListeners().filter(_.isInstanceOf[LinkListener])
    	.map(_.asInstanceOf[FSMouseListener]).foreach(removeMouseTrackingListener(_))
    addMouseTrackingListener(linkHandler)
    
    //set URL
    setDocument(welcomeURL.toExternalForm());    

  }


}