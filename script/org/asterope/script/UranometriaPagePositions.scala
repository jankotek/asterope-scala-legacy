package org.asterope.script

/**
 * This script is 'brutal force' approach to find best list positions
 * to match Uranometria
 *
 * There is only function which translates position to page number.
 * Rather than making inversion of it, I use brutal force to find best position for each page.
 *
 * Result of this script is than copy&pasted into code
 *
 */

import org.asterope.util._
import org.asterope.data._
import org.asterope.healpix._
import org.asterope.chart._


object UranometriaPagePositions{

  val allPoints:Iterable[Vector3d] = (0L to Pixelization.maxPixNumber)
        .map(Pixelization.ipix2Vector(_)) //convert to unit vector

  val fovs:Seq[Angle] =  10.degree.until(20.degree, 20.arcMinute)
  val landscapes = List(true,false)

  case class Page(sheet:Int, pos:Vector3d, fov:Angle,portrait:Boolean)

  def main(args:Array[String]){
    println("Time :" + stopWatch{
    val maxList = 473

    val pages:Seq[Page] = (1 to maxList).map{sheet=>
      //pixels which are on given uranometria page
      val points:Iterable[Vector3d] = allPoints
        .filter(sheet == AtlasChart.uranometria(_)) //only those on uranometria sheet

      val best:Page = {
        for (
          //creates combinations of positions, angle and landscape
          //use iterator on fovs, so no full solution has to be calculated
          angle <- fovs.iterator;    //need to determine smallest FOV, so it goes first
          center <- points;
          portrait <- landscapes;
          //creates chart with given coordinates
          w = if(portrait)210 else 148;
          h = if(portrait)148 else 210;
          chart = new Chart(position = center, fieldOfView = angle,width = w, height = h);
          //now test if all points are inside given canvas
          if(points.forall(chart.isInsideCanvas(_)))
        ) yield Page(sheet,center, angle,portrait)
      }.next //return only first from iterator
      best
    }

    //now print all stuff
    pages.foreach{p=>
      println("Page("+p.sheet+", Vector3d("+p.pos.x+","+p.pos.y+","+p.pos.z+"), Angle("+p.fov.uas+"L), "+p.portrait+"),");
    }


  })}
}