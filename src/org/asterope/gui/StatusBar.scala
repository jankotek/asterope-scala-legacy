package org.asterope.gui

import java.awt.event._
import org.asterope.util._
import javax.swing._
import event.{ChangeEvent, ChangeListener}

class StatusBar extends JPanel{


    setLayout(MigLayout("fill, insets 0 3 0 3"))
    val statusLabel = new JLabel()
    add(statusLabel, "growx")
    addSeparator()
    add(new StatusBarMemoryUsage, "w 80")

		//show menu tooltips in status bar
		MenuSelectionManager.defaultManager().addChangeListener(toolTipMenuListener)
		//TODO add this for toolbar when created

  def addSeparator(){
    val s = new JSeparator(SwingConstants.VERTICAL)
    add(s,"growy")
  }


	/** Shows menu tooltips on status bar */
	private object toolTipMenuListener extends ChangeListener{

		def getTooltip(source:Object):Option[String] = {
			if(source.isInstanceOf[JComponent]){
				val s = source.asInstanceOf[JComponent].getToolTipText
				if(s!=null) return Some(s)
			}
			if(source.isInstanceOf[AbstractButton]){
				val b = source.asInstanceOf[AbstractButton]
				if(b.getAction!=null && b.getAction.toolTip!=null){
					return Some(b.getAction.toolTip)
				}
			}
			None
		}

		def stateChanged(e:ChangeEvent){


			if(e.getSource.isInstanceOf[MenuSelectionManager])
			  onEDT{ //fire little bit latter, so menu traversal is fast
				val man = e.getSource.asInstanceOf[MenuSelectionManager]
				if(man.getSelectedPath!=null && !man.getSelectedPath.isEmpty){
					val item = man.getSelectedPath.last.getComponent
					val t = getTooltip(item);
					statusLabel.setText(t.getOrElse(""))
				}else{
					statusLabel.setText("")
				}
			}
		}

	}


  }

class StatusBarMemoryUsage extends  JLabel{

  def update(){
    val max = (Runtime.getRuntime.totalMemory() * 1e-6).toInt
    val used = (max - Runtime.getRuntime.freeMemory() * 1e-6).toInt
    val str = used + "M / "+max+"M"
    setText(str)
  }


  /** run garbage collector, triggered when user click on this progressbar*/
  val runGC = act{
    fork("runGC"){
      System.gc()
    }
  }

  //on mouse click call gc
  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent){
      runGC.call()
    }
  })


  //run timer and update status every 2 sec
  protected val timer = new javax.swing.Timer(1000, Bind.ActionListener(update()))
  timer.setRepeats(true)
  timer.start()


}