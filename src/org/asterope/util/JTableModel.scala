package org.asterope.util

import collection.mutable.{ArrayBuffer, Buffer}
import javax.swing.table._
import javax.naming.OperationNotSupportedException

/**
 * Swing `TableModel` which implements Scala mutable buffer.
 * It makes it easy to manipulate Table content using Scala Collections
 * It also implements change listeners, so changes in collection are reflected on screen. 
 */
abstract class JTableModel[E] (columns:Seq[String])
  extends AbstractTableModel
  with Buffer[E]{


  /** holds underlying data */
  private val data = new ArrayBuffer[E]

  /**
   * Override this to provide data for each column
   * @param e row object
   * @param column index
   * @return value displayed in table at given row and column
   */
  def getData(e:E,column:Int):Object

  override def getRowCount = data.size
  override def getColumnCount = columns.size
  override def getColumnName(i:Int) = columns(i)
  override def getColumnClass(i:Int) = classOf[Object]
  override def isCellEditable(row:Int,col:Int) = false

  override def getValueAt(row:Int, col:Int) = {
    val e = data(row)
    getData(e,col)
  }


  override def setValueAt(aValue: AnyRef, rowIndex: Int, columnIndex: Int){
    throw new OperationNotSupportedException()
  }

  def remove(n:Int) = {
    val elem = data.remove(n)
    fireTableRowsDeleted(n,n)
    elem
  }

  def insertAll(n: Int, elems: Traversable[E]){
    val oldSize = size;
    data.insertAll(n,elems)
    fireTableRowsInserted(n,n+(size-oldSize)-1)
  }

  def clear() {
    if(isEmpty)return
    val oldSize = size -1
    data.clear()
    fireTableRowsDeleted(0,oldSize)
  }

  def +=:(elem:E) = {
    data.`+=:`(elem)
    fireTableRowsInserted(0,0)
    this
  }

  def +=(elem:E) ={
    val pos = size
    data+=elem
    fireTableRowsInserted(pos,pos)
    this
  }

  def length = data.length

  def update(n: Int, newElem: E){
    data.update(n,newElem)
    fireTableCellUpdated(n,n)
  }

  def apply(n:Int):E = data.apply(n)

  def iterator:Iterator[E] = data.iterator

}