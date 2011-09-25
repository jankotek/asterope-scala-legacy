package org.asterope.gui

import org.asterope.util._
import org.asterope.chart.ChartSkyview._

class SkyviewFormTest extends GuiTestCase{

  val form = new SkyviewForm
  def parent = form.getParent.getParent

  def testFields{
    val m1 = new Memento
    var m2:Memento = m1;
    fork("test"){
      m2=Form.showDialog(m1,form).get
    }
    sleep
    onEDTWait{
      findJComboBox(parent,"survey").getEditor.setItem("dss2r")
      findJComboBox(parent,"scale").setSelectedItem(ScaleEnum.Linear)
      findJComboBox(parent,"resample").setSelectedItem(ResampleEnum.NN)
      findButton(parent,"okButton").doClick()
    }
    sleep

    assert(m2.survey === "dss2r")
    assert(m2.scale === ScaleEnum.Linear)
    assert(m2.resample === ResampleEnum.NN)

  }


}