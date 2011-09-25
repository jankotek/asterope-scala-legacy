package org.asterope.chart

import edu.umd.cs.piccolo.PNode
import org.asterope.util._

trait ChartPainter[C,E] {
	
	def defaultConfig:C;
	
	
	def paintObject(chart:ChartBase, config:C, obj:E,addToLayer:Boolean=true):Option[PNode]
	
	def paintAll(chart:ChartBase, config:C, objects:Iterator[E]){
		objects.foreach{ o=>
      checkInterrupted()
			paintObject(chart,config,o)
		}
	}
	
	def paintAll(chart:ChartBase, config:C, objects:Iterable[E]){
		paintAll(chart,config,objects.iterator)
	}
}