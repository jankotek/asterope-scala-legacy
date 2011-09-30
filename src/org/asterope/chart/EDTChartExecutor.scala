package org.asterope.chart

import org.asterope.util._

/**
 * Chart Executor which runs all actions inside EDT thread.
 * It has two phases:
 * First new Chart is created of screen, all tasks are executed in synchronized method
 *
 * Secondly Chart is added into PCanvas and displayed into screen.
 * In this case code must be executed inside EDT, as it causes repaints
 *
 */
class EDTChartExecutor extends ChartExecutor{
  /**indicates if Chart is connected with GUI.
   * If false, code does not have to be executed inside EDT thread
   */
  private var plugged = false;

  /**call before PCamera is added into PCanvas, after this is called all code is
   * executed inside EDT thread
   */
  def plugIntoSwing(){
    plugged = true
  }

  override def async(block: =>Unit){
    if(plugged) onEDT(block)
    else synchronized{block}
  }

  override def sync[E](block: =>E):E = {
    if(plugged) onEDTWait(block)
    else synchronized{block}
  }
}


