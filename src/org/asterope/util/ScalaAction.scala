package org.asterope.util


import javax.swing._
import org.asterope.gui._
import org.jdesktop.swingx.icon.EmptyIcon


/**
 * Class which wraps javax.swing.Action and provides some additional get/set methods
 *   
 * @see javax.swing.Action
 */
case class ScalaAction(peer:Action) {
  
  private def toOption[E] (e:Object):Option[E] = 
	  if(e == null) None
	  else Some(e.asInstanceOf[E])
  
  private def ifNull[E] (e:Object, default:E):E = 
	  if(e == null) default 
	  else e.asInstanceOf[E]
 
  
  /**
   * Title is not optional.
   */
  def title: String = ifNull(peer.getValue(javax.swing.Action.NAME),"")
  def title_=(t: String) { peer.putValue(javax.swing.Action.NAME, t) }
  
  def largeIcon: Icon = ifNull(peer.getValue(javax.swing.Action.LARGE_ICON_KEY),ScalaAction.emptyIcon)
  def largeIcon_=(i: Icon) { peer.putValue(javax.swing.Action.LARGE_ICON_KEY, i) }
  def smallIcon: Icon = ifNull(peer.getValue(javax.swing.Action.SMALL_ICON), ScalaAction.emptyIcon)
  def smallIcon_=(i: Icon) { peer.putValue(javax.swing.Action.SMALL_ICON, i) }
  
  /**
   * For all components.
   */
  def toolTip: String = 
    ifNull(peer.getValue(javax.swing.Action.SHORT_DESCRIPTION), "") 
  def toolTip_=(t: String) { 
    peer.putValue(javax.swing.Action.SHORT_DESCRIPTION, t) 
  }
  /**
   * Can be used for status bars, for example.
   */
  def longDescription: String = 
    ifNull(peer.getValue(javax.swing.Action.LONG_DESCRIPTION), "") 
  def longDescription_=(t: String) { 
    peer.putValue(javax.swing.Action.LONG_DESCRIPTION, t) 
  }
  
  /**
   * Default: java.awt.event.KeyEvent.VK_UNDEFINED, i.e., no mnemonic key.
   * For all buttons and thus menu items.
   */
  def mnemonic: Int = ifNull(peer.getValue(javax.swing.Action.MNEMONIC_KEY), 
                             java.awt.event.KeyEvent.VK_UNDEFINED)
  def mnemonic_=(m: Int) { peer.putValue(javax.swing.Action.MNEMONIC_KEY, m) }
  
  /*/**
   * Indicates which character of the title should be underlined to indicate the mnemonic key.
   * Ignored if out of bounds of the title string. Default: -1, i.e., ignored. 
   * For all buttons and thus menu items.
   */
   1.6: def mnemonicIndex: Int = 
   ifNull(peer.getValue(javax.swing.Action.DISPLAYED_MNEMONIC_INDEX_KEY), -1)
   def mnemonicIndex_=(n: Int) { peer.putValue(javax.swing.Action.DISPLAYED_MNEMONIC_INDEX_KEY, n) }
  */
  
  /**
   * For menus.
   */
  def accelerator: Option[KeyStroke] = 
    toOption(peer.getValue(javax.swing.Action.ACCELERATOR_KEY))
  def accelerator_=(k: Option[KeyStroke]) { 
    peer.putValue(javax.swing.Action.ACCELERATOR_KEY, k orNull)
  } 
  
  /**
   * For all components.
   */
  def enabled: Boolean = peer.isEnabled 
  def enabled_=(b: Boolean) { peer.setEnabled(b) }
  
 
  
  /**
   * Only honored if not <code>None</code>. For various buttons.
   */ 
   def selected: Option[Boolean] = toOption(peer.getValue(javax.swing.Action.SELECTED_KEY))
   def selected_=(b: Option[Boolean]) { 
	  peer.putValue(javax.swing.Action.SELECTED_KEY, 
                 if (b == None) null else new java.lang.Boolean(b.get)) 
  }
   
  /** call and execute this action. Usefull for testing */
  def call() {
	if(isEDT)
		peer.actionPerformed(null);
	else
		onEDTWait{
			peer.actionPerformed(null);
		}
  }

   
}

object ScalaAction{
  val emptyIcon = new EmptyIcon()
}