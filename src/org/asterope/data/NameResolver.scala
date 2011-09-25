package org.asterope.data

import org.asterope.util._


class NameResolver(ds:DeepSkyDao, stars:LiteStarDao) {


  def resolve(name:String):NameResolverResult = {

    var r = List[NameResolverResult]()

    //deepsky
    r ++= ds.objectsByName(name).map{n=>
      val desc = DeepSky.resourceMap.getString(n.deepSkyType.toString)
      new NameResolverResult(Some(n.vector),Some(desc),name)
    }
    //stars
    r ++= stars.objectsByName(name).map(n=>new NameResolverResult(Some(n.vector),Some("Star"),name))

    //TODO seq versus option
    r.foreach{s=>
      return s
    }
    new NameResolverResult(queryString = name)
  }


}


case class NameResolverResult(pos:Option[Vector3d]=None,description:Option[String]=None, queryString:String){

  def constel = Constel.constelOnPosition(pos.get)
}