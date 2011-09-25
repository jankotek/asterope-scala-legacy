package org.asterope.util

import javax.swing.JTextArea
import org.asterope.gui._

class FormTest  extends GuiTestCase{

  case class Memento(s1:String = "aa")

  object form extends JTextArea with Form[Memento]{

    setName("s1TextArea")

    def reset(m:Memento) = setText(m.s1)
    def commit = new Memento(s1 = getText)
  }

  def ok = findButton(form.getParent.getParent,"okButton")
  def cancel = findButton(form.getParent.getParent,"cancelButton")
  def textArea = findJTextArea(form.getParent.getParent,"s1TextArea")

  def testShowDialogButtons(){
    //test there is Ok and Cancel button on dialog form
    onEDT{
      Thread.sleep(100)
      Form.showDialog(new Memento,form)
    }
    waitForWindow()

    //test there is OK button
    assert(ok.isEnabled)
    assert(ok.isVisible)
//    assert(ok.getText === "OK")

    assert(cancel.isEnabled)
    assert(cancel.isVisible)
//    assert(cancel.getText === "Cancel")
    onEDTWait{ok.doClick()}

  }

  def testShowDialogCommit(){
    val mem  = new Memento("CDR")
    var mem2:Memento = null
    onEDT{
      Thread.sleep(100)
      mem2 = Form.showDialog(mem,form).get
    }
    waitForWindow()
    onEDTWait{textArea.setText("CDR2")}
    onEDTWait{ok.doClick()}
    waitUntil("dialog was closed",mem2 != null)

    assert(mem2.s1 === "CDR2")
  }

  def testShowDialogCancel(){
    val mem  = new Memento("CDR")
    var mem2:Option[Memento] = null
    onEDT{
      Thread.sleep(100)
      mem2 = Form.showDialog(mem,form)
    }
    waitForWindow()
    onEDTWait{textArea.setText("CDR2")}
    onEDTWait{cancel.doClick()}
    waitUntil("dialog was closed",mem2 != null)

    assert(mem2 === None)

  }

}