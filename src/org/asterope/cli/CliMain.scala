package org.asterope.cli

/**
 * Main object which handles CLI.   
 * This starts IOC context and collect all `CliFeature` classes. Then it passes parameters to right class. 
 * 
 * @author Jan Kotek
 *
 */
object CliMain {
	
	def main(args:Array[String]){
		
		val features = List(new CliChart)
		if(features.size == 0){
			//no feature found, display help
			if(args.size>0)
				System.err.println("Unknown feature keyword: "+args(0))
			System.err.println("Supported features: ")
			features.foreach{cli=>
				System.err.println(cli.keyword+"\t - "+cli.description)
			}
		}else if(features.size>1){
			//handles duplicates, should be very rare
			System.err.println("More then one CLI feature found for keyword: "+args(0))
			features.foreach{cli=>
				System.err.println(cli.keyword+"\t - "+cli.description)
			}
		}else{
			//pass parameters to feature
			val feature = features.head
			//remove first item in args
			val args2 = args.toList.takeRight(args.size -1).toArray
			feature.call(args2)
		}
		//TODO shutdown CLI feature (it implements Beans and have RecordManager)
	}
}


/**
 * An 'feature' called from command line. Each represents different set of functionality (charts, data exports, ephems, GUI...) 
 */
trait CliFeature{
	def description:String;
	def keyword:String;
	def call(args:Array[String])	
}