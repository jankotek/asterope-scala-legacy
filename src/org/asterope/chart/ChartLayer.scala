/*
 *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */
package org.asterope.chart;

import edu.umd.cs.piccolo.PLayer
import java.util.{Comparator, Collections}

import edu.umd.cs.piccolo.PNode
import collection.mutable.WeakHashMap;



/**
 * Piccolo layer with thread assertions. It can also arrange nodes by Z-Order parameter 
 */
case class ChartLayer(layerName:ChartLayers.Value) 
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