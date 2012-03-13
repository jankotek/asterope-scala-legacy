package org.asterope.gui

import net.infonode.docking.util._
import net.infonode.docking.theme.ShapedGradientDockingTheme
import net.infonode.util.Direction
import org.asterope.util._
import collection.mutable.WeakHashMap
import net.infonode.docking._
import java.awt.{CardLayout, BorderLayout, Component, Dimension}
import javax.swing._

/**
 * Main window with frame docks
 */
class MainWindow (val resmap:ResourceMap){

  val menu:JMenuBar = new JMenuBar


  lazy val mainFrame = new JFrame(){
    // ImageJ and other libraries are leaving windows behind
    // DISPOSE_ON_CLOSE does not really work
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE)
    val c = new JPanel();
    c.setLayout(new BorderLayout)
    c.add(rootWindow, BorderLayout.CENTER)
    c.add(statusBar, BorderLayout.SOUTH)
    setContentPane(c)
    setPreferredSize(new Dimension(1000, 600))
    setName("mainWindow")
    if(menu!=null)
      setJMenuBar(menu)
  }

  protected lazy val viewMap = new StringViewMap()

  protected lazy val rootWindow = {
    val r = DockingUtil.createRootWindow(viewMap,true)
    //disable shadows and lot of other stuff. It has to be explicitely enabled
    val sup = new ShapedGradientDockingTheme().getRootWindowProperties
    sup.getDockingWindowProperties.setCloseEnabled(false)
    sup.getDockingWindowProperties.setMinimizeEnabled(false)
    sup.getDockingWindowProperties.setMaximizeEnabled(false)
    sup.getDockingWindowProperties.setUndockEnabled(false)
    sup.getDockingWindowProperties.setDockEnabled(false)
    sup.getDockingWindowProperties.setDragEnabled(false)
    sup.getDockingWindowProperties.setRestoreEnabled(false)
    sup.getDockingWindowProperties.setUndockOnDropEnabled(false)
    r.getRootWindowProperties.addSuperObject(sup)
    //set basic layout
    r.setWindow(new SplitWindow(true,0.25f,
      new SplitWindow(false,0.6f,leftTopTabs,leftBottomTabs),
      editorTabs))
    r.getWindowBar(Direction.DOWN).setEnabled(true);

    r
  }

  lazy val editorTabs = new TabWindow(){
    getWindowProperties.setMaximizeEnabled(true)
    getWindowProperties.setRestoreEnabled(true)
  }



  protected lazy val leftTopTabs = new TabWindow(){
  }
  protected lazy val leftBottomTabs = new TabWindow(){
  }

  protected def bottomBar = rootWindow.getWindowBar(Direction.DOWN)

  protected lazy val statusBar = new StatusBar

  def show(){
    if(menu!=null)
      resmap.injectComponents(menu)

    resmap.injectActionFields(this)
    mainFrame.pack()
    resmap.injectComponents(mainFrame)
    mainFrame.setVisible(true)
  }

  def showMinimized(){
    mainFrame.setState(java.awt.Frame.ICONIFIED)
    show();
  }

  def hide(){
    mainFrame.setVisible(false)
  }


  def addEditor(id:String, comp:JComponent):View = {
    resmap.injectComponents(comp)
    resmap.injectActionFields(comp)
    val v = new View(id, null, comp)
    v.getWindowProperties.setCloseEnabled(true)
    v.getWindowProperties.setMaximizeEnabled(true)
    viewMap.addView(id, v)
    editorTabs.addTab(v)
    v
  }

  protected def fabSideView(id:String, comp:JComponent):View = {
    assert(viewMap.getView(id) == null, "View with id '"+id+"' is already present")
    resmap.injectActionFields(comp)
    resmap.injectComponents(comp)

    val title = resmap.getString(id+".text")
    val v = new View(title, null, comp)
    v.getWindowProperties.setCloseEnabled(true)
    v.getWindowProperties.setMinimizeEnabled(true)
    v.getWindowProperties.setRestoreEnabled(true)
    v.getWindowProperties.setDockEnabled(true)
    v.getWindowProperties.setUndockEnabled(true)
    v.getWindowProperties.setDragEnabled(true)
    v.getWindowProperties.setUndockOnDropEnabled(true)
    viewMap.addView(id, v)

    v
  }

  def addBottomBarView(id:String, comp:JComponent):View ={
    val v = fabSideView(id,comp);
    bottomBar.addTab(v)
    v
  }

  def addLeftTopView(id:String, comp:JComponent):View = {
    val v = fabSideView(id,comp)
    leftTopTabs.addTab(v)
    v

  }

  def addLeftBottomView(id:String, comp:JComponent):View = {
    val v = fabSideView(id,comp)
    leftBottomTabs.addTab(v)
    v
  }

  def getFocusedEditor = {
    val v = editorTabs.getSelectedWindow
    if(v != null) v.asInstanceOf[View].getComponent
    else null
  }




  /**
   * An view which contents depends on active editor
   */
  abstract class EditorBoundView extends JLayeredPane{

    def editorOpened(editor:Component):JComponent
    def editorClosed(editor:Component,  subview:JComponent)

    private val cardLayout = new CardLayout()

    setLayout(cardLayout)
    add(new JPanel(),"empty")

    private val editor2subview = new WeakHashMap[Component,  (String,JComponent)]

    private def editorChanged(editor:Component){
      if(editor == null){
        cardLayout.show(this,"empty")
        return;
      }

      if(!editor2subview.contains(editor)){
        val key = math.random.toString
        val subview = editorOpened(editor)

        if(subview!=null){
          editor2subview.put(editor,(key,subview))
          add(subview,key)
        }else{
          cardLayout.show(this,"empty")
          return;
        }
      }

      cardLayout.show(this,editor2subview(editor)._1)
      revalidate()

    }

    editorTabs.addListener(new DockingWindowAdapter{
      override def viewFocusChanged(previouslyFocusedView: View, focusedView: View){
        if(focusedView==null) editorChanged(null)
        else editorChanged(focusedView.getComponent)
      }

      override def windowAdded(addedToWindow: DockingWindow, addedWindow: DockingWindow){
        editorChanged(addedWindow.asInstanceOf[View].getComponent)
      }

      override def windowClosed(window: DockingWindow){
        val win = window.asInstanceOf[View].getComponent;
        if(editor2subview.contains(win)){
          remove(editor2subview(win)._2)
          editor2subview.remove(win)
        }
      }



    });
  }



}