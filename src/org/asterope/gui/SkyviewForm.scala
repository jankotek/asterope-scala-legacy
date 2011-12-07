package org.asterope.gui

import org.asterope.chart._
import skyview.executive.Settings
import org.asterope.chart.Skyview._
import org.asterope.util._
import collection.JavaConversions._
import javax.swing._
import io.Source
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator
import java.awt.{Dimension, Component}
import org.jdesktop.swingx.renderer.{StringValue, DefaultListRenderer}

/**
 * Form which drives Skyview on map
 */

class SkyviewForm extends Form[SkyviewConfig] {

    protected val surveyModel:JListModel[String] =  {
    val vals = for(s<-Skyview.surveys;s2<-s._2) yield s2._1
    new JListModel[String](vals.toList)
  }

  protected val survey = new JComboBox(surveyModel) {
    setName("survey")
    AutoCompleteDecorator.decorate(this)
  }

  protected val scaleModel = {
    val vals = ScaleEnum.values.toList
    new JListModel[ScaleEnum.Value](vals)
  }

  protected val scaleCombo = new JComboBox(scaleModel) {
    setName("scale")
  }

  protected val resampleModel = new JListModel[ResampleEnum.Value](
    ResampleEnum.values.toList
  )

  protected val resampleCombo = new JComboBox(resampleModel){
    setName("resample")
  }
  
  protected val internalEngineCheck = new JCheckBox{
    setName("skyview.useInternalEngine")
  }


  protected val lutModel =  new JListModel[Luts.Lut](Luts.luts)

  protected object lutCombo extends JComboBox(lutModel){


    val rendererX = new DefaultListRenderer(
      Bind.stringValue[Luts.Lut](_.name),
      Bind.iconValue[Luts.Lut](_.colorBar))
    setRenderer(rendererX.asInstanceOf[ListCellRenderer[Luts.Lut]])
    setName("lut")

  }



  setLayout(MigLayout())
  add(new JLabel().withName("skyview.survey"))
  add(survey, "growx,wrap")
  add(new JLabel().withName("skyview.scale"))
  add(scaleCombo, "growx,wrap")
  add(new JLabel().withName("skyview.resample"))
  add(resampleCombo, "growx,wrap")
  add(new JLabel().withName("skyview.lut"))
  add(lutCombo, "growx,wrap")
  add(new JLabel())
  add(internalEngineCheck, "growx,wrap")
    

  def reset(e:SkyviewConfig){
    survey.getEditor.setItem(e.survey)
    scaleModel.setSelectedItem(e.scale)
    resampleModel.setSelectedItem(e.resample)
    lutModel.setSelectedItem(Luts.luts.find(_.id == e.lut).get)
    internalEngineCheck.setSelected(e.useInternalEngine)    
  }

  def commit() = new SkyviewConfig(
    survey = surveyModel.getSelectedItem2,
    scale = scaleModel.getSelectedItem2,
    resample = resampleModel.getSelectedItem2,
    lut = lutModel.getSelectedItem2.id,
    useInternalEngine = internalEngineCheck.isSelected   
  )


}

protected[gui] object Luts {

  case class Lut(id:String, name:String, colorBar:ImageIcon)

  val luts:List[Lut] = {
    val folder = "skyviewColorBars/";
    Source.fromURL(getClass.getClassLoader.getResource(folder+"colors.csv")).getLines
      .map(_.split(";"))
      .map{l=>
        val colorBarRes = folder+l(2)
        val colorBar =  new ImageIcon(getClass.getClassLoader.getResource(colorBarRes))
        Lut(l(0),l(1),colorBar)
    }.toList

  }

}


object SkyviewProgressDialog extends JDialog{
  setName("chartSkyviewProgressDialog")
  setTitle("Skyview survey")  
  setModal(true)
  val output = new JTextArea()
  val cancelButton = new JButton("Cancel") //TODO externalize
  Bind.action(cancelButton,{
    cancelButton.setEnabled(false)
    cancelButton.setText("Cancelling...") //TODO externalize
    skyview.executive.Settings.cancel = true;
    fork{
      //wait until process was really canceled, when it is done variable is set back to false
      while(skyview.executive.Settings.cancel == true) Thread.sleep(1)
      //now close dialog
      onEDT{
        SkyviewProgressDialog.setVisible(false)
      }
    }
  })
  setContentPane(new JPanel{
    setLayout(MigLayout("fill"))
    add(new JScrollPane(output),"wrap,grow")
    add(new JSeparator,"wrap,growx")
    add(cancelButton,"align center")

  })
  setPreferredSize(new Dimension(600,500))
  pack();

  //redirect skyview output
  object outputStream extends java.io.OutputStream{
    def write(b: Int){
      onEDT{
        val s = Character.toString(b.asInstanceOf[Char])
        val doc = output.getDocument
        doc.insertString(output.getText.length,s,null)
        output.setCaretPosition(output.getText.length - 1)
      }
    }
  }
  skyview.executive.Settings.out = new java.io.PrintStream(outputStream)
  skyview.executive.Settings.err = new java.io.PrintStream(outputStream)


  override  def show(){
    output.setText("")
    cancelButton.setText("Cancel")
    cancelButton.setEnabled(true)
    super.show();
  }
}

