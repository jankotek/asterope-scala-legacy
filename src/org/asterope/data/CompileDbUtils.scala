package org.asterope.data;

import java.io._
import collection.mutable.ListBuffer
import io.Source
import org.asterope.util._
import org.asterope.geometry.CoordinateSystem


/** Varius utilities for CompileDb class*/
object CompileDbUtils{




  /** read constellation boundary lines from file and return result in iterator */
  def readConstelBounds:List[SkyLine] = {

    def roundAngle(v:Double):Angle = {
      val v2 = math.round(v * 60D).toLong //round to arc seconds
      return v2.arcMinute
    }

    val BOUNDS_FILE = new File("data/constellations_bound_18.dat");
    /** rotater to transform vector2Rade from J2000 to J1875 */
    val j1875rotater = CoordinateSystem.factory("J1875").getRotater
    /** rotater to transform vector2Rade from J1875 to J2000 */
    val j1875derotater = j1875rotater.inverse
    val northPole = Vector3d(j1875derotater.transform(Vector3d.northPole.toArray))

    case class Point(ra:Angle,de:Angle, constel:String)
    val buf = ListBuffer[SkyLine]()

    /** read and parseFormat lines */
    val points:List[Point] = Source.fromFile(BOUNDS_FILE).getLines.map{st=>
      val ss = st.split(" ").filter(_.size>1)
      Point(roundAngle(ss(0).toDouble * 15), roundAngle(ss(1).toDouble), ss(2))
    }.toList

    //list of all constellations
    val constels = points.map(_.constel).toSet
    assert(constels.size==89) //Oph have two separated regions
    //iterate over all constels and construct lines
    for(
      constel <- constels;
      points2 = points.filter(_.constel == constel);
      i <- 0 until points2.size;
      p1 = points2(i);
      p2 = points2((i+1)%points2.size); //close line by moving to 0 point when modulo overflows
      if(p1.de != (-90).degree && (p2.de != (-90).degree)); //for some reasons south pole is included, skip it
      if(!(p1.de == 88.degree && p2.de== 88.degree && constel == "Cep"));  //one duplicate line near north pole, filter it
      v1 = Vector3d.rade2Vector(p1.ra,p1.de); //start point of line
      v2 = Vector3d.rade2Vector(p2.ra,p2.de); //end point of line
      q1 = Vector3d(j1875derotater.transform(v1.toArray));  //line start point in J1875
      q2 = Vector3d(j1875derotater.transform(v2.toArray))   //line end point in J1875
    ){

      val line = if(p1.ra == p2.ra){
        //boundary line follows RA line. Can use great circle
        if(p1.de>p2.de)
          TwoPointSkyLine(q1,q2)
        else
          TwoPointSkyLine(q2,q1)
      } else {
          if(p1.ra<p2.ra){
            val angle = p2.ra-p1.ra
            if(angle>180.degree)
              RotatingSkyLine(q2, northPole, 360.degree - angle)
            else
              RotatingSkyLine(q1, northPole, angle)
          }else if(p2.ra<p1.ra){
            val angle = p1.ra-p2.ra
            if(angle>180.degree)
              RotatingSkyLine(q1, northPole, 360.degree - angle)
            else
              RotatingSkyLine(q2, northPole, angle)
          }
          else
            throw new Error("should not be here")
      }

      //sanity check
      assert((line.start.angle(q1)<Angle.R2M && line.end.angle(q2)<Angle.R2M) ||
        (line.start.angle(q2)<Angle.R2M && line.end.angle(q1)<Angle.R2M))
      buf+=line
    }

    assert(buf.size > points.size - 20) //there are some lines skipped on south pole
    //at bounds there are two constellation and each have boundary line
    //each line would be painted twice,
    //so use Set to eliminate most of duplicates
    buf.toSet.toList
  }


}
