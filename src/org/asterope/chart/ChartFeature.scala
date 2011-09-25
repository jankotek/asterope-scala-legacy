package org.asterope.chart

import org.asterope.util._

/**
 * Provides general functionality class for ChartBase. 
 * Implementations draw stars, objects, grids, skyview images etc
 * 
 * Each type of feature should use it's own layer (stars, deepsky, milkyway, coordinate grid). 
 * But for example variable stars, should use 'stars' layer.  
 * 
 * All implementation must be thread safe and respect threaded access to ChartControl
 * @tparam E type of config. Is external case class which stores configuration  
 */
trait ChartFeature[E] {
	
	/**
	 * ChartFeature should not contain any shared data (any state). 
	 * All settings should be stored in `config` object.
	 * This way settings are separated from actual 'hard data',
	 * can be serialized and manipulated externally.  
	 *  
	 * @return default settings for this feature
	 */
	def defaultConfig:E;

	/**
	 * Update chart with this feature. 
	 * If this feature provides stars, it should paint them in this method
	 * 
	 * This method must wait until update is finished.
	 *  
	 * @param chart to paint on
	 * @param config stores configuration for this action
	 */
	def updateChart(chart: ChartBase, config:E=defaultConfig);
	

	/**
	 * Remove data from this feature from chart.   
	 * Reverts 'updateMap' function. Usually it just clears layer. 
	 * 
	 * This method must wait until clear is finished.
	 *  
	 * @param mc MapControl to revert from  		
	 */
	def clearChart(chart: ChartBase);
	
	
}
