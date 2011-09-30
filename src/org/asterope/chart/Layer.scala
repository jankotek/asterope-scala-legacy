package org.asterope.chart

import edu.umd.cs.piccolo.{PNode, PLayer}
import collection.mutable.WeakHashMap
import edu.umd.cs.piccolo.PNode._
import java.util.{Collections, Comparator}

object Layer extends Enumeration {

	val bg, milkyway, skyview, 
			coordinateGridJ2000, coordinateGridJ1950, 
			coordinateGridGalactic, coordinateGridEcliptic,
			constelLine, constelBoundary,
			deepsky, star, 
			label,legend,  fg = Value


}

/**
 * Piccolo layer with thread assertions. It can also arrange nodes by Z-Order parameter
 */
case class Layer(layerName:Layer.Value)
	extends PLayer {


    override def addChild(arg0:PNode) {
        assertThread();
        super.addChild(arg0);
    }

    override def repaint() {
        assertThread();
        super.repaint();
    }


    /**
     * stores zorders for nodes
     */
    private val zorders = new WeakHashMap[PNode, Double]();

    /**
     * add node to layer with defined z-order
     *
     * @param child  node to add
     * @param zorder, can be null
     */
    def addChildWithZorder(child:PNode, zorder:Double) {
        if (child == null)
            throw new IllegalArgumentException("child is null");
        assertThread();

       zorders.put(child, zorder);
       val children = getChildrenReference().asInstanceOf[java.util.List[PNode]];

       val comparator = new Comparator[PNode](){
           override def  compare( o1:PNode, o2:PNode):Int= {
               val zorder1:Option[Double] = zorders.get(o1);
               val zorder2:Option[Double] = zorders.get(o2);
               if((zorder1.isEmpty && zorder2.isEmpty ))
                   return 0;
               else if(zorder1.isEmpty || zorder1.get < zorder2.get)
                   return -1;
               else if(zorder2.isEmpty || zorder2.get < zorder1.get)
                   return 1;
               else
                   return 0;
           }
       }

       var i:Int = Collections.binarySearch(children, child, comparator);
       if(i<0) i = -i -1;

       addChild(i, child);

    }

    protected def assertThread() {
        //subclass may add thread assertion here
    }

}