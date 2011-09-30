package org.asterope.chart

import edu.umd.cs.piccolo.PNode
import java.awt.Color
import org.asterope.data._
import java.awt.Font
import edu.umd.cs.piccolo.nodes._
import org.asterope.util._
import java.awt.image.BufferedImage

/**
 * Configuration object `ChartLabels`
 */
case class ChartLabelsConfig(
			showStarLabels:Boolean = true,
			showDeepSkyLabels:Boolean = true,
	        numberOfStarLabels:Int = 20,
	        numberOfDeepSkyLabels:Int = 50,
	        labelFont:Font = PText.DEFAULT_FONT 
	);


class ChartLabels(
		)extends ChartFeature[ChartLabelsConfig] {

	def defaultConfig = new ChartLabelsConfig
	def clearChart(chart: ChartBase){
		chart.getLayer(Layer.label).removeAllChildren()
	}
	
	def updateChart(chart: ChartBase, config:ChartLabelsConfig){
		val objs:List[(HasNomenclature,PNode)] = findStarsToLabel(chart,config ) ::: findDeepSkyToLabel(chart,config)


		chart.executor.sync{ //this all must be on EDT thread, otherwise chart would repaint many times and cause poor performance
			var size:Float = config.labelFont.getSize;
      objs.foreach{case (starlinkObj,node) =>
        checkInterrupted()
        if(size>4)
          size = size - 0.03F
				val name = selectName(starlinkObj)
				val label= new PText(name)
				label.setFont(config.labelFont.deriveFont(size))
				label.setTextPaint(chart.colors.fg); //TODO special color for labels
				//find best position for label
          
				val pos = findBestLabelPosition(chart, node, label.getWidth.toInt, label.getHeight.toInt);
			
				pos.foreach{p=>
					label.setGlobalTranslation(p);
					//add label and wait until done				
					chart.addNode(Layer.label,label, async=false)
				}
			}
		}
	}
	
	/**
	 * Find brightest stars on chart, for which label is added. 
	 */
	def findStarsToLabel(chart: ChartBase, config:ChartLabelsConfig):List[(LiteStar,PNode)] = {
			if(!config.showStarLabels) return Nil
			val liteStars = chart.objects
					.filter(_.isInstanceOf[LiteStar])
					.map(_.asInstanceOf[LiteStar])
					.filter{s=> chart.isInsideCanvas(chart.getNodeForObject(s).get)}
                    .filter(_.names.forall(_.toString.startsWith("UCAC"))) //TODO primitive hack not to show UCAC star labels for Galway Sky Catalog
					.toList
					.sortWith(_.mag<_.mag) 
					.take(config.numberOfStarLabels)
			liteStars.map{s=>
				val node = chart.getNodeForObject(s).get
				(s,node)
			}
	}
	
	/**
	 * Find brightest deepsky object on chart 
	 */
	def findDeepSkyToLabel(chart: ChartBase, config:ChartLabelsConfig):List[(DeepSky,PNode)] = {
			if(!config.showDeepSkyLabels) return Nil
			val ds = chart.objects
					.filter(_.isInstanceOf[DeepSky])
					.map(_.asInstanceOf[DeepSky])
					.filter(!_.names.isEmpty) //only show stars with labels
					.filter{s=> chart.isInsideCanvas(chart.getNodeForObject(s).get)}
					.toList
					.sortWith(_.mag<_.mag) 
					.take(config.numberOfDeepSkyLabels)
			ds.map{s=>
				val node = chart.getNodeForObject(s).get
				(s,node)
			}
	}

	/**
 	 * Select best label for given StarlinkObject.   
	 */
	def selectName(starlinkObj:HasNomenclature):String = {
			//TODO better algorihm at selectName
			//TODO make selectName configurable

      //select catalog name
			starlinkObj.names.filter(_.formatId == 0).foreach(s=> return s.toString)

    	//find NGC catalog name
			starlinkObj.names
				.filter(_.catalogPrefix == "NGC")
				.foreach{c=>
					//find messier name
					val messier = starlinkObj.names.find(_.catalogPrefix == "M")
					val suffix = if(messier.isDefined) " "+messier.get.toString.replace(" ","")else ""
					return c.toString.replace("NGC","").trim + suffix
				}

			//find Messier catalog name
			starlinkObj.names.find(_.catalogPrefix == "M").foreach{c=>
				return c.toString.replaceAll(" ([0-9]+)","$1")
			}


			val flamsteed = starlinkObj.names
				.find(_.formatId == 70) //TODO remove hardcoded formatId
				


			starlinkObj.names
				.find(n=> n.formatId == 68 || n.formatId == 69) //TODO remove hardcoded formatId				
				.foreach{n=>
					return n.toString.replaceAll(" [a-zA-Z]+$","") +
						(if(flamsteed.isDefined)(" "+flamsteed.get.toString.replaceAll(" [a-zA-Z]+$","")) else"")

				}

			flamsteed.foreach{n=>
				return n.toString.replaceAll(" [a-zA-Z]+$","")
			}



      //find HIP names
      starlinkObj.names
			  .find(n=> n.formatId == 20 || n.formatId == 21) //TODO remove hardcoded formatId
			  .foreach{n=>
					return n.toString.replaceAll("HIP ","H")
				}


      //select shortest name
			starlinkObj.names.map(_.toString).sortWith(_.size<_.size).head
	}
	
	
  /**Find best position where to put label. It uses fuzzy algorithm to find
   * place where label does not overlap with other things
   * @param chart ChartBase on which label should be placed
   * @param node PNode for which label should be placed
   * @param w width of label
   * @param h height of label
   * @return best position for label  
   */
  def findBestLabelPosition(chart:ChartBase, node:PNode, w:Int, h:Int):Option[Point2d] = {
	  
	  /*
	   * Prepare image 
	   */
	  val offset = 3;
	  val bounds = node.getFullBounds;
	  val imgX = math.floor(bounds.getMinX - w - offset).toInt;
	  val imgY = math.floor(bounds.getMinY - h - offset).toInt;
	  val imgW =  math.ceil(bounds.getWidth + w*2 + offset*2).toInt;
	  val imgH = math.ceil(bounds.getHeight + h*2 + offset*2).toInt;
	  val img = chart.toBufferedImage(imgX, imgY, imgW, imgH);
	   
	  /*
	   * make candidate points with scores 
	   */
	  val bgColor = chart.colors.bg
	  val lx:Double = imgW/2 - w/2;
	  val ly:Double = imgH/2 - h/2;
	  val viewBounds = chart.camera.getViewBounds;
	  var bestPos:Point2d = null;
	  var bestScore = java.lang.Double.MAX_VALUE;

    //bitmap is copied to this array, so new instance does not have to be instanciated each time
    val rgbArray = Array.ofDim[Int](w*h)

	  for{
	 	  	x:Int <- Range(0, imgW-w, math.max(imgW/20,2));
	 	  	if(imgX +x >viewBounds.getMinX);
	 	  	if(imgX + x + w < viewBounds.getMaxX);
	 		y:Int <- Range(0, imgH-h, math.max(imgH/20,2));
	 	  	if(imgY +y >viewBounds.getMinY);
	 	  	if(imgY + y + h < viewBounds.getMaxY);
	 		distPenalty = math.abs((lx - x) * (lx - x)+(ly - y) * (ly - y));
	 		imgScore = calculateColorScore(img,bgColor,x,y,w,h,rgbArray);
	 		totalScore = imgScore + distPenalty * 20;
	 		if(bestScore>totalScore || bestPos == null)
	  }{
			bestPos = new Point2d(x,y);
			bestScore = totalScore;
	  }
	  if(bestPos == null)
		  None;
	  else
		  Some(new Point2d(imgX+bestPos.x, imgY + bestPos.y));
  }

    /**
   * Calculate color score. Is based on distance from background color and how often color 'swaps'
   * Is used to calculate 'score' for label position.
   * Position with smallest difference from bg color is used.
   *
   * @param img to calculate average color on
   * @param bg  background color
   * @return number from -255-255 which represents avg color distance
   */
  def calculateColorScore(img: BufferedImage, bg: Color, x: Int, y: Int, w: Int, h: Int, rgbArray:Array[Int]): Double = {
    var ret: Long = 0
    val red: Int = bg.getRed
    val green: Int = bg.getGreen
    val blue: Int = bg.getBlue
    img.getRGB(x, y, w, h, rgbArray, 0, w)
    var oldRgb: Int = 0
    val size = rgbArray.size
    var i = 0;
    while(i<size){
      val rgb = rgbArray(i)
      //this loop has to be very optimized, no external method can be called from here
      if (oldRgb != rgb) {

        val redDist: Int = red - ((rgb >> 16) & 0xFF)
        val greenDist: Int = green - ((rgb >> 8) & 0xFF)
        val blueDist: Int = blue - (rgb & 0xFF)
        ret += redDist * redDist
        ret += greenDist * greenDist
        ret += blueDist * blueDist
      }
      oldRgb = rgb
      i = i + 1;
    }
    ret
  }

}