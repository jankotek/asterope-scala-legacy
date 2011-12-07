package org.asterope.gui

import org.asterope.util._
import javax.swing._

class MainWindowTest extends GuiTestCase{

  val mainWin = beans.mainWin



  def testViewTitleLocalization(){
    onEDTWait{
      mainWin.showMinimized();
      val v = mainWin.addLeftBottomView("testView",new JLabel("alala"))
      assert(v.getTitle ==="Localized title")
    }
  }


  /**
   * Test that action shown in main window is forwarded to editor
   */
  def testEditorForwardAction(){
    onEDT{
      mainWin.showMinimized();
    }


    trait Actions{
      val action:Action
    }

    object mainWindowActions {
      val action = new EditorBoundAction(mainWin)
    }

    var counter1 = 0

    object editor1 extends JLabel("editor1") with Actions{
      val action = mainWindowActions.action.editorAction(this){
        counter1 += 1
      }
    }

    object editor2 extends JLabel("editor2")

    var counter3 = 0
    object editor3 extends JLabel("editor3") with Actions{
      val action = mainWindowActions.action.editorAction(this){
        counter3 += 1
      }
    }


    onEDTWait{
      beans.resmap.injectActionFields(mainWindowActions)
      beans.resmap.injectActionFields(editor1)

      mainWin.addEditor("editor1",editor1)
      waitUntil(mainWin.getFocusedEditor == editor1)




    //check that call is forwared to editor
    assert(counter1 === 0)
    mainWindowActions.action.call()
    assert(counter1 === 1)
    assert(counter3 === 0)

    //check that editor 'enabled' is forwarded
    assert(mainWindowActions.action.enabled)
    editor1.action.enabled = false
    assert(!mainWindowActions.action.enabled)

    //check that editor 'selected' is forwarded
    assert(mainWindowActions.action.selected === None)
    assert(editor3.action.selected === None)
    editor1.action.selected = Some(true)
    assert(editor3.action.selected === None)
    assert(mainWindowActions.action.selected.get == true)
    editor1.action.selected = Some(false)
    assert(mainWindowActions.action.selected.get == false)
    assert(editor3.action.selected === None)
    editor1.action.selected = Some(true)


    //now activate new view, mainWindowActions should be disabled

    mainWin.addEditor("editor2",editor2)

    waitUntil(mainWin.getFocusedEditor == editor2)
    assert(mainWindowActions.action.selected === None)
    assert(!mainWindowActions.action.enabled)


    mainWin.addEditor("editor3",editor3)

    waitUntil(mainWin.getFocusedEditor == editor3)
    assert(mainWindowActions.action.enabled)

    //check that call is forwarded to third editor
    counter1 = 0
    counter3 = 0
    mainWindowActions.action.call()
    assert(counter1 === 0)
    assert(counter3 === 1)

    //check that editor 'enabled' is forwarded
    assert(mainWindowActions.action.enabled)
    editor1.action.enabled = false
    assert(mainWindowActions.action.enabled)
    editor3.action.enabled = false
    assert(!mainWindowActions.action.enabled)
    }
  }
}