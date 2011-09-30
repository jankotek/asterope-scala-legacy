package org.asterope.gui

import net.infonode.docking.util._
import net.infonode.docking._
import net.infonode.docking.theme.ShapedGradientDockingTheme
import net.infonode.util.Direction
import java.beans.PropertyChangeEvent
import javax.swing._
import java.awt.{BorderLayout, Component, Dimension}
import org.asterope.util._

/**
 * Main window with frame docks
 */
trait MainWindow {

  CheckThreadViolationRepaintManager.hook()

  val resourceMap = new ResourceMap(classOf[MainWindow]);

  val menu:JMenuBar



  protected lazy val mainFrame = new JFrame(){
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

  protected lazy val editorTabs = new TabWindow(){
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
      resourceMap.injectComponents(menu)

    resourceMap.injectActionFields(this)
    mainFrame.pack()
    resourceMap.injectComponents(mainFrame)
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
    resourceMap.injectComponents(comp)
    resourceMap.injectActionFields(comp)
    val v = new View(id, null, comp)
    v.getWindowProperties.setCloseEnabled(true)
    v.getWindowProperties.setMaximizeEnabled(true)
    viewMap.addView(id, v)
    editorTabs.addTab(v)
    v
  }

  protected def fabSideView(id:String, comp:JComponent):View = {
    assert(viewMap.getView(id) == null, "View with id '"+id+"' is already present")
    resourceMap.injectActionFields(comp)
    resourceMap.injectComponents(comp)

    val title = resourceMap.getString(id+".text")
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

  def editorForwardAction(clazz:Class[_]):Action = {
    var mainWinAction:Action = null;

    def editorAction(f:Component):Action = {
      assert(mainWinAction.getValue("baseName")!=null,"could not get 'baseName', had you call ResourceMap.injectActionFields()?")
      val name = mainWinAction.getValue("baseName").toString
      val field = f.getClass.getDeclaredField(name)
      field.setAccessible(true)
      field.get(f).asInstanceOf[Action]
    }

    mainWinAction = act{
      val f = getFocusedEditor
      assert(clazz.isAssignableFrom(f.getClass), "Editor is not instance of required class:\n"+f+"\n"+clazz)
      //use reflection to get into field with the same name as this action
      editorAction(getFocusedEditor).call()
    }


    /**
     * Code which synchronizes some action properties
     * - when active editor changes, it enables or disables mainWinAction
     * - keeps mainWinAction 'enabled' and 'selected' properties in sync
     */
    var oldEditorAction:Action = null;

    object actionListener extends java.beans.PropertyChangeListener{

      def propertyChange(evt: PropertyChangeEvent){
        if(evt.getPropertyName == Action.SELECTED_KEY || evt.getPropertyName == "enabled")
          mainWinAction.putValue(evt.getPropertyName,evt.getNewValue);
      }
    }

    /** called when active editor changes */
    def rehookActionListeners(f:java.awt.Component){
      val newAction:Action =
        if(clazz.isAssignableFrom(f.getClass)) editorAction(f)
        else null

      mainWinAction.enabled = newAction!=null && newAction.enabled
      mainWinAction.selected = if(newAction==null) None else newAction.selected
      if(oldEditorAction!=null)
        oldEditorAction.removePropertyChangeListener(actionListener)
      if(newAction!=null)
        newAction.addPropertyChangeListener(actionListener)
      oldEditorAction = newAction;
    }

    editorTabs.addListener(new DockingWindowAdapter{
      override def windowAdded(addedToWindow: DockingWindow, addedWindow: DockingWindow){
        if(addedWindow.isInstanceOf[View])
          rehookActionListeners(addedWindow.asInstanceOf[View].getComponent)
      }
      override def windowClosed(window: DockingWindow){
        rehookActionListeners(getFocusedEditor)
      }

      override def viewFocusChanged(previouslyFocusedView: View, focusedView: View){
        if(focusedView!=null)
          rehookActionListeners(focusedView.getComponent)
      }

    })

    mainWinAction
  }

}

