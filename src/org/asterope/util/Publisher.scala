package org.asterope.util

import org.asterope.gui._
import org.asterope.util._
import java.lang.System

/** 
 *    Shorter way to write publisher / listener model. 
 *    Provider exposes this class as public immutable field (val).
 *    When event happens, it uses any of 'fireXX' methods to notify 
 *    subscribers. 
 *       
 *    Name of public val should describe when Publisher is triggered. 
 *    In general it should start with 'onXXX' prefix. 
 *    Good names are for example: 'onMouseEntered', 'onChartRefreshFinished'
 *    
 *    Main disadvantage of this class is that 'fireXX' methods are exposed
 *    and can be triggered by some other code. 
 *    Also there is no code to remove listener 
 *     
 *    
 *       
 *    
 *    Example howto use it:
 *    <code>
 *    	//initialize in class which provides listeners 
 *    	val onMouseEntered = new Publisher
 *    	
 *    	//fire event and call listeners 
 *    	onMouseEntered.firePublish
 *    
 *    	//subscribe listener on client which wants to be notified
 *    	onMouseEntered{
 *    		//this code block is called when  
 *    		println("mouse entered")
 *    	}	 
 *    
 *    	
 *    
 *    
 *  
*/
class Publisher[E] {
	
	protected var listeners = List[(E)=>Unit]();  

	/**
	 * Register given code block to be executed when Publisher is fired. 
	 *
   * In this case block is executed in the same thread as `fire`
	 */
	def listen(block:(E) =>Unit){
     synchronized{
		  listeners = block :: listeners;
	   }
  }

  def apply(block:(E) =>Unit){
    listen(block)
  }

  /**
   * Register code block to be executed when Publisher is fired.
   *
   * Listener is called in EDT thread in separate event.
   */
	def listenInEDT(block:(E) =>Unit){
		listen{e=>
      onEDT(block(e))
    }
	}

  /**
   * Register code block to be executed when Publisher is fired.
   *
   * Listener is called in newly forked thread
   */
	def listenInFork(block:(E) =>Unit){
		listen{e=>
      fork("listen"+System.currentTimeMillis()){
        block(e)
      }
    }
	}

	
	/**
	 * Execute all registered listeners in this thread, block until all done
	 */
	def firePublish(e:E){
    listeners.foreach(_(e));
  }
	


	
}