package org.asterope.util

import javax.swing._
import javax.swing.text._
import javax.swing.event._
import java.awt.event._
import java.awt.Component
import org.jdesktop.swingx.renderer.{IconValue, StringValue}

object Bind{
/**
 * bind JTextComponent to Publisher. Publisher is fired every time text component changes
*/
def change(j:JTextComponent, block: =>Unit){
  j.getDocument.addDocumentListener(new DocumentListener(){

     def insertUpdate(e:DocumentEvent){
        onEDT(block)
     }

     def removeUpdate(e:DocumentEvent){
       onEDT(block)
     }

     def changedUpdate(e:DocumentEvent){
       onEDT(block)
     }
  });
}

def change[E](j:JList[E], block: =>Unit){
  j.addListSelectionListener(new ListSelectionListener(){
    override def valueChanged(e:ListSelectionEvent){
      onEDT(block)
    }
  })
}

def change(j:JTable, block: =>Unit){
  j.getSelectionModel.addListSelectionListener(new ListSelectionListener(){
    override def valueChanged(e:ListSelectionEvent){
      onEDT(block)
    }
  })
}


def change(j:JSlider, block: =>Unit){

  j.addChangeListener(new ChangeListener(){
    override def stateChanged(e: ChangeEvent){
      onEDT(block)
    }
  })

}


//  /** bind EventList to component */
//  def bindChange[E](el: ca.odell.glazedlists.EventList[E], jl:JList){
//	  jl.setModel(new  ca.odell.glazedlists.swing.EventListModel[E](el));
//  }
def action[E](comp:JComboBox[E],  block: =>Unit) {
  comp.addActionListener(new ActionListener(){
     override def actionPerformed(e:ActionEvent) {
        onEDT(block)
     }
  })
}

def action(comp:JTextComponent, block: =>Unit) {

   comp.addKeyListener(new KeyAdapter() {
       override def keyPressed( e:KeyEvent) {
         if (e.getKeyCode == KeyEvent.VK_ENTER)
           onEDT(block)
       }
   });
}


def action(comp:Component, block: =>Unit) {
  if(comp.isInstanceOf[AbstractButton]){
    comp.asInstanceOf[AbstractButton].addActionListener(ActionListener(block))
    return
  }

   comp.addKeyListener(new KeyAdapter() {
       override def keyPressed( e:KeyEvent) {
         if (e.getKeyCode == KeyEvent.VK_ENTER)
           onEDT(block)

         }
       }
    );

   comp.addMouseListener(new MouseAdapter() {
     override def mouseClicked(e: MouseEvent) {
         if (e.getClickCount == 2)
           onEDT(block)
     }
   });
}

  /** converts blocks to ActionListener */
  def ActionListener( block: =>Unit) = new ActionListener {
    def  actionPerformed(e:ActionEvent){
      block
    }
  }

  def delayed(delay:Long, coalesce:Boolean = false, f: =>Unit):Runnable = {
    val timer = new javax.swing.Timer(delay.toInt, Bind.ActionListener(f))
    timer.setRepeats(false)
    timer.setCoalesce(coalesce)
    org.asterope.util.Runnable(onEDTWait{
      timer.restart()
    })
  }

  def stringValue[E](e: E=>String) = new StringValue{

    override def getString(value: AnyRef): String = {
      e(value.asInstanceOf[E])
    }
  }


  def iconValue[E](e: E=>Icon) = new IconValue{

    override def getIcon(value: AnyRef): Icon = {
      e(value.asInstanceOf[E])
    }
  }

}
//
//implicit def fcToChangeListener(f: => Any):ChangeListener= new ChangeListener(){
//
//
//override def stateChanged(e:ChangeEvent) {
//     f
//  }
//}
