package org.asterope.util

import javax.swing._
import collection.mutable.{ArrayBuffer, Buffer}
import event.{ListDataEvent, ListDataListener}

/**
 * Swing `ComboBoxModel` and 'ListModel' which implements Scala muttable 'Buffer'.
 * This makes very easy to manipulate JList content using Scala collections
 *
 * This model also implements change listeners, so changes are reflected on screen.
 */
class JListModel[E] extends Buffer[E]
    with ListModel[E]
    with ComboBoxModel[E]  {
  
	def this(i:Traversable[E]) = { 
	  this();
	  insertAll(0,i)
	}

    /** holds underlying data */
    private val data = new ArrayBuffer[E]

    def getSize: Int = data.size

    def getElementAt(index: Int) = data.apply(index)

    /** listeners notified when list changes */
    private var listeners: List[ListDataListener] = Nil

    def addListDataListener(l: ListDataListener){
      listeners = l :: listeners
    }

    def removeListDataListener(l: ListDataListener){
      listeners = listeners.filter(_!=l)
    }

    private var selectedItem:AnyRef = null

    def setSelectedItem(anItem: AnyRef){
      selectedItem = anItem;
    }

    def getSelectedItem = selectedItem
    def getSelectedItem2:E = selectedItem.asInstanceOf[E]

    def remove(n:Int) = {
      val elem = data.remove(n)
      val e = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,n,n)
      listeners.foreach(_.contentsChanged(e))
      elem
    }

    def insertAll(n: Int, elems: Traversable[E]){
      val oldSize = size;
      data.insertAll(n,elems)
      val e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,n,n+(size-oldSize)-1)
      listeners.foreach(_.contentsChanged(e))
    }

    def clear() {
      if(isEmpty)return
      val oldSize = size -1
      data.clear()
      val e = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,0,oldSize)
      listeners.foreach(_.contentsChanged(e))
    }

    def +=:(elem:E) = {
      data.`+=:`(elem)
      val e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,0,0)
      listeners.foreach(_.contentsChanged(e))
      this
    }

    def +=(elem:E) ={
      val pos = size
      data+=elem
      val e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,pos,pos)
      listeners.foreach(_.contentsChanged(e))
      this
    }

    def length = data.length

    def update(n: Int, newelem: E){
      data.update(n,newelem)
      val e = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,n,n)
      listeners.foreach(_.contentsChanged(e))
    }

    def apply(n:Int):E = data.apply(n)

    def iterator:Iterator[E] = data.iterator

  }