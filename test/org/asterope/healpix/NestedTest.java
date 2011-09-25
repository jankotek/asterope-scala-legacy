package org.asterope.healpix;

import junit.framework.TestCase;

public class NestedTest extends TestCase {

    /**
     * tests conversion from nest schema pixel to ring schema pixel
     * @throws Exception
     */
    public void testNest2Ring() throws Exception {

            int ipnest = 3;
            int nside = 2;
            int ipring = (int) Nested.nest2ring(nside, ipnest);
            assertEquals("ipring=" + ipring, 0, ipring, 1e-10);
            ipnest = 0;
            nside = 2;
            ipring = (int) Nested.nest2ring(nside, ipnest);
            assertEquals("ipring=" + ipring, 13, ipring, 1e-10);
            ipnest = 18;
            nside = 2;
            ipring = (int) Nested.nest2ring(nside, ipnest);
            assertEquals("ipring=" + ipring, 27, ipring, 1e-10);
            ipnest = 23;
            nside = 2;
            ipring = (int) Nested.nest2ring(nside, ipnest);
            assertEquals("ipring=" + ipring, 14, ipring, 1e-10);
            ipnest = 5;
            nside = 4;
            ipring = (int) Nested.nest2ring(nside, ipnest);
            assertEquals("ipring = " + ipring, 27, ipring, 1e-10);
            System.out.println(" test Nest2Ring is done");
    }

    /**
     * tests conversion from ring schema pixel to nest schema pixel
     * @throws Exception
     */
    public void testRing2Nest() throws Exception {

            System.out.println(" start test Ring2Nest !!!!!!!!!!!!!!!!!!!!!!");
            int ipring = 0;
            int nside = 2;

            int ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest=" + ipnest, 3, ipnest, 1e-10);
            ipring = 13;
            nside = 2;
            ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest=" + ipnest, 0, ipnest, 1e-10);
            ipring = 27;
            nside = 2;
            ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest=" + ipnest, 18, ipnest, 1e-10);
            ipring = 14;
            nside = 2;
            ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest=" + ipnest, 23, ipnest, 1e-10);
            ipring = 27;
            nside = 4;
            ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest = " + ipnest, 5, ipnest, 1e-10);
            ipring = 83;
            nside = 4;
            ipnest = (int) Nested.ring2nest(nside, ipring);
            assertEquals("ipnest = " + ipnest, 123, ipnest, 1e-10);
            System.out.println(" test Ring2Nest is done");
    }

}
