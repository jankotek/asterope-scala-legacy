package org.asterope.gui

import org.asterope.util._
import org.asterope.data._

class SearchDialogTest extends GuiTestCase
  with DataBeans with TestRecordManager{
  lazy val resmap = new ResourceMap(classOf[MainWindow])

  lazy val form = new SearchDialog(nameResolver,resmap)

  def text = findJTextField(form, "idText")
  def result = findJLabel(form, "resultLabel")

  def testBlankDisabled(){
    onEDTWait{
      form.reset(new NameResolverResult(queryString = ""))
    }
    assert(form.okAction.enabled === false)
    assert(result.getText === "Nothing found.")
  }

  def testAsterope(){
    onEDTWait{
      form.reset(new NameResolverResult(queryString = "Asterope"))
      assert(form.okAction.enabled === true)
      assert(result.getText === "Found Star in Tau.")
      val r = form.commit
      assert(r.pos.get.angle(Vector3d.asterope).radian < 1.arcMinute)
      assert(r.description.get === "Star")
    }
  }


}