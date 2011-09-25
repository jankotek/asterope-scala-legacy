package org.asterope.gui

import java.util.logging._
import java.util.Date
import org.jdesktop.swingx.JXTable
import org.jdesktop.swingx.decorator._
import javax.swing.table.DefaultTableCellRenderer
import java.io._
import org.asterope.util._
import javax.swing._
import java.awt.{Color, Component}

/**
 * View which shows error, warnings and info messages in MainWindow
 *
 * It also shows general error popup when an exception is thrown
 */
class MessageView(resMap:ResourceMap) extends JComponent{

  /** columns displayed in table */
  protected lazy val colums = Vector(resMap.getString("levelColumn"),
    resMap.getString("timeColumn"),resMap.getString("messageColumn"))

  /** model for log table */
  object data extends JTableModel[LogRecord](colums){

    override def getData(e:LogRecord,column:Int) = {
      column match{
        case 0 => e.getLevel
        case 1 => new Date(e.getMillis)
        case 2 => getMessage(e)
      }
    }
  }


  /** dummy action used to inject icon*/
  val messageViewSevereIcon = act{}
  /** dummy action used to inject icon*/
  val messageViewWarningIcon = act{}
  /** dummy action used to inject icon*/
  val messageViewInfoIcon = act{}


  /** log table shown in view */
  object table extends JXTable(data){
    //make first two column with fixed size
    setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN)
    getColumnModel.getColumn(0).setPreferredWidth(90)
    getColumnModel.getColumn(0).setMinWidth(90)
    getColumnModel.getColumn(1).setPreferredWidth(200)
    getColumnModel.getColumn(1).setMinWidth(200)
    getColumnModel.getColumn(2).setPreferredWidth(10000)

    //make error rows red
    object errorPredicate extends HighlightPredicate{
      override def isHighlighted(renderer:Component, adapter:ComponentAdapter) = {
        data(adapter.row).getLevel == Level.SEVERE
      }
    }
    setHighlighters(new ColorHighlighter(
      errorPredicate,getBackground,Color.RED))

    //add icons to TYPE column
    object iconCellRenderrer extends DefaultTableCellRenderer{
      override def getTableCellRendererComponent(
                                                  table:JTable, value:Object,isSelected:Boolean, hasFocus:Boolean, row:Int, column:Int):Component = {
        val s = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column).asInstanceOf[JLabel]
        val icon =
          if(value == Level.SEVERE) messageViewSevereIcon.smallIcon
          else if(value == Level.WARNING) messageViewWarningIcon.smallIcon
          else if(value == Level.INFO) messageViewInfoIcon.smallIcon
          else  null

        s.setIcon(icon)
        s
      }
    }
    getColumn(0).setCellRenderer(iconCellRenderrer)
  }

  val messageViewClear = act{
    data.clear()
  }

  /** construct GUI */
  setLayout(MigLayout("fill, flowy, nogrid, insets 0 0 0 0"))
  add(new JButton(messageViewClear))
  add(new JLabel,"growy, wrap" )
  add(new JScrollPane(table),"grow")

  /** plug into Asterope Logger */
  protected object MessageViewLogHandler extends Handler{
    def flush(){}
    def close(){}

    def publish(record: LogRecord){
      onEDT{
        data+=record
        if(record.getLevel == Level.SEVERE){
          showDetail(record)
        }
      }
    }
  }
  Log.logger.addHandler(MessageViewLogHandler)

  /** converts LogRecord into message displayed in table*/
  protected def getMessage(rec:LogRecord)={
    if(rec.getThrown == null){
      if(rec.getMessage!=null) rec.getMessage
      else "unknown message!"
    }else{
      if(rec.getMessage!=null && rec.getThrown.getMessage == null){
        rec.getMessage + " - "+ rec.getThrown.getClass.getName
      }else{
        rec.getMessage + " - "+ rec.getThrown.getClass.getName +":"+rec.getThrown.getMessage
      }
    }
  }

  //show details after double click
  Bind.action(table, {
    if(table.getSelectedRow != -1){
      showDetail(data(table.getSelectedRow))
    }
  })

  /** convert LogRecord into html displayed in detail dialog */
  def makeHtml(sel:LogRecord) = {
    def entry(title:String,str:String,pre:Boolean) = {
      "<b>"+title+"</b><br/><table><tr><td>&nbsp;&nbsp;&nbsp;</td><td>"+
        (if(pre) "<pre>"+str+"</pre>" else str)+
        "</td></tr></table>"
    }

    val thrown:String =
      if(sel.getThrown==null) null
      else{
        val st = new ByteArrayOutputStream()
        sel.getThrown.printStackTrace(new PrintStream(st))
        new String(st.toByteArray)
      }


    val text = ""+
      entry(resMap.getString("message"),sel.getMessage,false)+
      entry(resMap.getString("time"),new Date(sel.getMillis).toString,false)+
      entry(resMap.getString("severity"),sel.getLevel.toString,false)+
      (if(thrown!=null) entry(resMap.getString("exception"),thrown,true) else "")
    text
  }


  /** show dialog with details of an LogRecord */
  def showDetail(msg:LogRecord){

    object comp extends JScrollPane with Form[String]{
      val editor = new JEditorPane("text/html","")
      editor.setEditable(false)
      setViewportView(editor)

      //user can copy text into clipboard
      val toolTipCopyAction = act{
        editor.selectAll()
        editor.copy()
        editor.select(0,0)
      }

      def reset(e:String){
        editor.setText(e)
        editor.setCaretPosition(0)
      }
      def commit = editor.getText
    }
    resMap.injectActionFields(comp)
    resMap.injectComponents(comp)

    val text = makeHtml(msg)
    Form.showDialog(text,comp, showCancel = false,
      buttonsExtra = List(new JButton(comp.toolTipCopyAction)),
      width=600, height=400
    )

  }
}


