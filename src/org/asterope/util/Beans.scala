package org.asterope.util

/**
 * An trait which provides primitive IOC
 */

trait Beans{

  private var _onShutdownHooks:List[()=>Unit] = Nil

  /** Custom code which will be executed on shutdown (when shutdown() method is called */
  def onShutdown(block: =>Unit){
    if(_onShutdownHooks == null)
      throw new IllegalAccessException("Shotdown already started, can not add shutdown hook")
    _onShutdownHooks ::= (()=>block)
  }


  /** Call shutdown on all registered beans */
  final def shutdown(){
    val hooks = _onShutdownHooks;
    _onShutdownHooks = null
    hooks.foreach(_())
  }

}
