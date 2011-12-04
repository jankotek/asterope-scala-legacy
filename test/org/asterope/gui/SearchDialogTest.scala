package org.asterope.gui

import org.asterope.util._
import org.asterope.data._
import org.apache.commons.math.geometry.Vector3D

class SearchDialogTest extends GuiTestCase{
  lazy val resmap = new ResourceMap(classOf[MainWindow])

  lazy val form = new SearchDialog(beans.nameResolver,resmap)

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
      assert(Vector3D.angle(r.pos.get,Vector3D_asterope).radian < 1.arcMinute)
      assert(r.description.get === "Star")
    }
  }


}