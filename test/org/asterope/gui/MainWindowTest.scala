package org.asterope.gui

import org.asterope.util._
import javax.swing._

class MainWindowTest extends ScalaTestCase with MainWindow{

  override val resourceMap = new ResourceMap(classOf[MainWindowTest])
  val menu = new JMenuBar

  def testViewTitleLocalization(){
    onEDTWait{
      showMinimized();
      val v = addLeftBottomView("testView",new JLabel("alala"))
      assert(v.getTitle ==="Localized title")
    }
  }


  /**
   * Test that action shown in main window is forwarded to editor
   */
  def testEditorForwardAction(){
    onEDT{
      showMinimized();
    }


    trait Actions{
      val action:Action
    }

    object mainWindowActions {
      val action = new EditorBoundAction
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
      resourceMap.injectActionFields(mainWindowActions)
      resourceMap.injectActionFields(editor1)

      addEditor("editor1",editor1)
      waitUntil(getFocusedEditor == editor1)




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

    addEditor("editor2",editor2)

    waitUntil(getFocusedEditor == editor2)
    assert(mainWindowActions.action.selected === None)
    assert(!mainWindowActions.action.enabled)


      addEditor("editor3",editor3)

    waitUntil(getFocusedEditor == editor3)
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