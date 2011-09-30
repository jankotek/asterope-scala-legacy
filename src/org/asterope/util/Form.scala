package org.asterope.util

import javax.swing._
import java.awt.{Dimension, Component}

/**
 * An abstract form which can edit an config (case class)
 */

trait Form[E] extends JComponent{
  def reset(e:E)
  def commit:E

  val okAction = act{}
  val cancelAction = act{}
}

object Form{

  def showDialog[E](e:E, form:Form[E], showCancel:Boolean = true,
                    buttonsExtra:List[Component]=Nil,
                    width:Int = 400, height:Int = 300):Option[E] = {
    onEDTWait{
      val dialog = new JDialog();
      val okButton = new JButton(form.okAction){
        setName("okButton")
      }
      val cancelButton = new JButton(form.cancelAction){
        setName("cancelButton")
      }
      val panel = new JPanel{
        setLayout(MigLayout("fill"))
        add(form,"grow,spanx")
        add(new JSeparator,"growx,spanx")
        add(new JLabel,"growx")
        add(okButton, "w 20%")
        if(showCancel)
          add(cancelButton, "w 20%")
        buttonsExtra.foreach(add(_,"w 20%"))
      }
      dialog.setContentPane(panel);
      form.reset(e)
      dialog.setModal(true)
      dialog.getRootPane.setDefaultButton(okButton)

      //install close action
      var okPressed = false
      Bind.action(okButton,{
        okPressed = true
        dialog.setVisible(false)
      })
      Bind.action(cancelButton,{
        okPressed = false
        dialog.setVisible(false)
      })
      panel.setPreferredSize(new Dimension(width,height))
      dialog.pack();
      dialog.setVisible(true)
      //result
      if(okPressed)Some(form.commit);
      else None
    }
  }
}