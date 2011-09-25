package org.asterope.gui

import org.asterope.util._
import org.asterope.data._
import org.jdesktop.swingx.JXHyperlink
import javax.swing._

/**
 * User enters object name and it is resolved to celestial position.
 */
class SearchDialog(resolver:NameResolver, resmap:ResourceMap)
  extends JPanel with Form[NameResolverResult]{


  setLayout(MigLayout())

  add(new JLabel().withName("SearchDialog.id"))
  val idText = new JTextField().withName("idText");
  //delayed action which updates result label
  val delayedUpdate = Bind.delayed(200,true,{
    okAction.enabled = false;
    updateResult()
  })
  Bind.change(idText, delayedUpdate.run())
  add(idText,"w 100%,wrap")

  add(new JLabel().withName("SearchDialog.example"),"spanx,wrap")
  add(new JXHyperlink().withName("SearchDialog.exampleLink"),"spanx,wrap")

  add(new JSeparator,"spanx, growx, wrap")

  val resultLabel = new JLabel().withName("resultLabel")
  add(resultLabel,"spanx")

  def reset(m:NameResolverResult){
    idText.setText(m.queryString)
    idText.selectAll()
    updateResult()
  }
  def commit = lastResult


  private var lastResult:NameResolverResult = new NameResolverResult(queryString = "");

  okAction.enabled = false
  def updateResult(){
    lastResult = resolver.resolve(idText.getText)
    okAction.enabled = lastResult.pos.isDefined
    resultLabel.setText(
      if(lastResult.pos.isEmpty) resmap.getString("SearchDialog.notFound")
      else resmap.getString("SearchDialog.found",lastResult.description.get, lastResult.constel)
    )
  }

}

