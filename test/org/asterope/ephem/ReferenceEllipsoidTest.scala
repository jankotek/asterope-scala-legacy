package org.asterope.ephem

import org.asterope.util.ScalaTestCase

class ReferenceEllipsoidTest extends ScalaTestCase{

  def testReferenceEllipsoid{
    println("ReferenceEllipsoid Test");

    val obs = ObserverElement.MADRID

    val ref = new ReferenceEllipsoid(ReferenceEllipsoid.LATEST);
    val obs_geodetic = ref.geodeticToGeocentric(obs);
    val obs_geocentric = ref.geocentricToGeodetic(obs_geodetic);

    println(obs.name);
    println(obs.longitude);
    println(obs.latitude);
    println(obs.height);

    //TODO not sure those values are correct, were printed when test has runned first time
    assert(obs.longitude === -0.06475171524898962)
    assert(obs.latitude === 0.7054620864589786)
    assert(obs.height === 693)

    println(obs_geocentric.name);
    println(obs_geocentric.longitude);
    println(obs_geocentric.latitude);
    println(obs_geocentric.height);

   //TODO there is no difference between geodetic and geocentric coordinates
    assert(obs_geocentric.longitude === -0.06475171524898962)
    assert(obs_geocentric.latitude === 0.7054620864589786)
    assert(obs_geocentric.height === 693)

  }

}
