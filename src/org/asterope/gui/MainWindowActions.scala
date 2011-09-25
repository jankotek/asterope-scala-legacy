package org.asterope.gui

import org.asterope.data._
import org.asterope.util._
import org.asterope.chart.ChartBeans


trait MainWindowActions extends ChartEditorFab{

  val resourceMap:ResourceMap
  val chartBeans:ChartBeans

  var lastSearchText = "";
  val searchObject = act{
    object d extends SearchDialog(chartBeans.nameResolver, resourceMap)
    resourceMap.injectComponents(d)
    resourceMap.injectActionFields(d)
    val res = Form.showDialog(new NameResolverResult(None, None,lastSearchText),d)
    if(res.isDefined){
      lastSearchText = res.get.queryString
      openChartOnObject(lastSearchText)
    }
  }

}