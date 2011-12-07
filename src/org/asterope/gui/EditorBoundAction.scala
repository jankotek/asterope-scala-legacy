package org.asterope.gui

import javax.swing.{Action, AbstractAction}
import java.awt.Component
import java.beans.PropertyChangeEvent
import org.asterope.util._
import net.infonode.docking.{View, DockingWindow, DockingWindowAdapter}
import java.awt.event.ActionEvent


/**
 * Action which is displayed in main menu, but forwards all functionality to an editor.
 * If editor is not opened, action is disabled.
 * Action atributes (enabled, selected) are synchronized with editor.
 */
class EditorBoundAction(mainWin:MainWindow) extends AbstractAction{
  setEnabled(false)

  private var oldEditorAction:Action = null;

  private var listeners = Map[Component,Action]()

  def addActionListener(editor:Component,action:Action){
    listeners += editor->action
  }

  /*
   * Code which synchronizes some action properties
   * - when active editor changes, it enables or disables mainWinAction
   * - keeps this 'enabled' and 'selected' properties in sync
   */
  private object actionListener extends java.beans.PropertyChangeListener{

    def propertyChange(evt: PropertyChangeEvent){
      if(evt.getPropertyName == Action.SELECTED_KEY || evt.getPropertyName == "enabled")
        putValue(evt.getPropertyName,evt.getNewValue);
    }
  }

  /** called when active editor changes */
  private def rehookActionListeners(editor:Component){
    val newEditorAction:Action = listeners.get(editor).getOrElse(null)

    action2ScalaAction(this).enabled = (newEditorAction!=null && newEditorAction.enabled)
    action2ScalaAction(this).selected = (if(newEditorAction==null) None else newEditorAction.selected)
    if(oldEditorAction!=null)
      oldEditorAction.removePropertyChangeListener(actionListener)
    if(newEditorAction!=null)
      newEditorAction.addPropertyChangeListener(actionListener)
    oldEditorAction = newEditorAction;
  }

  mainWin.editorTabs.addListener(new DockingWindowAdapter{
    override def windowAdded(addedToWindow: DockingWindow, addedWindow: DockingWindow){
      if(addedWindow.isInstanceOf[View])
        rehookActionListeners(addedWindow.asInstanceOf[View].getComponent)
    }
    override def windowClosed(window: DockingWindow){
      rehookActionListeners(mainWin.getFocusedEditor)
    }

    override def viewFocusChanged(previouslyFocusedView: View, focusedView: View){
      if(focusedView!=null)
        rehookActionListeners(focusedView.getComponent)
    }

  })

  def actionPerformed(e:ActionEvent){
    //forward to editor
    if(oldEditorAction!=null){
      oldEditorAction.actionPerformed(e)
    }else{
      throw new Error("EditorBoundAction should not be called when no editor is active.")
    }
  }


  def editorAction(editor:Component)(block: => Unit):Action = {
    val action = act{block};
    //add listener from main window action
    this.addActionListener(editor,action)
    action
  }


}
