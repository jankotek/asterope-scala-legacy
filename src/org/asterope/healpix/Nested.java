package org.asterope.healpix;

/**
 * Contains conversion functions between RING and NESTED pixel numbering scheme.
 * We use RING and all NESTED related stuff was removed from source code.
 * But some external sources requires NESTED scheme.
 *
 */
public class Nested {

    protected static final  int xmax = 4096;
    protected static final int pixmax = 262144;
    protected static final int xmid = 512;

    // coordinates of lowest corner of each face
    private static final long[] jrll = { 0, 2, 2, 2, 2, 3, 3, 3, 3, 4, 4, 4, 4 }; // in units of NSIDE

    private static final long[] jpll = { 0, 1, 3, 5, 7, 0, 2, 4, 6, 1, 3, 5, 7 }; // in units of NSIDE/2



    protected static final long[] x2pix = new long[xmax+1];

    protected static final long[] y2pix = new long[xmax+1];

    protected static final long[] pix2x = new long[pixmax+1];

    protected static final long[] pix2y = new long[pixmax+1];

    static{
        mk_pix2xy();
        mk_xy2pix();
    }



    /**
     * converts pixel number from ring numbering schema to the nested one
     *
     * @param nside
     *            long resolution
     * @param ipring long pixel number in ring schema
     * @return long pixel number in nest schema
     * @throws IllegalArgumentException
     */
    public static long ring2nest(long nside, long ipring) {
                    long ipnest = 0;
                    double fihip;
                    double hip;
                    long npix, nl2, nl4, ncap, ip, iphi, ipt, ipring1, kshift, face_num;
                    long nr, irn, ire, irm, irs, irt, ifm, ifp, ix, iy, ix_low, ix_hi, iy_low;
                    long iy_hi, ipf;
                    String SID = "ring2nest:";
                    //
                    face_num = 0;
                    if ((nside < 1) || (nside > PixTools.ns_max())) {
                            throw new IllegalArgumentException(SID + " nside should be power of 2 >0 and < "+PixTools.ns_max());
                    }
                    npix = 12 * nside * nside; // total number of points

                    if ((ipring < 0) || (ipring > npix - 1)) {
                            throw new IllegalArgumentException(SID + " ipring out of range [0,npix-1]");
                    }

                    nl2 = 2 * nside;
                    nl4 = 4 * nside;
                    ncap = nl2 * (nside - 1); // points in each polar cap, =0 for nside = 1
                    ipring1 = ipring + 1;
                    // finds the ring number, the position of the ring and the face number
                    if (ipring1 <= ncap) { // north polar cap
                            hip = ipring1 / 2.0;
                            fihip = Math.floor(hip);
                            irn = (long)( Math.sqrt(hip - Math.sqrt(fihip))) + 1; // counted from
                            // north pole
                            iphi = ipring1 - 2 * irn * (irn - 1);

                            kshift = 0;
                            nr = irn; // 1/4 of the number of points on the current ring
                            face_num = (iphi - 1) / irn; // in [0,3 ]

                    } else if (ipring1 <= nl2 * (5 * nside + 1)) { // equatorial region
                            ip = ipring1 - ncap - 1;
                            irn = (long)(ip / nl4) + nside; // counted from north pole
                            iphi = (long) PixToolsUtils.MODULO(ip, nl4) + 1;

                            kshift = (long) PixToolsUtils.MODULO(irn + nside, 2); // 1 if odd 0
                            // otherwise
                            nr = nside;
                            ire = irn - nside + 1; // in [1, 2*nside+1]
                            irm = nl2 + 2 - ire;
                            ifm = (iphi - ire / 2 + nside - 1) / nside; // face boundary
                            ifp = (iphi - irm / 2 + nside - 1) / nside;
                            if (ifp == ifm) {
                                    face_num = (long) PixToolsUtils.MODULO(ifp, 4.) + 4;
                            } else if (ifp + 1 == ifm) { // (half-) faces 0 to 3
                                    face_num = ifp;
                            } else if (ifp - 1 == ifm) { // (half-) faces 8 to 11
                                    face_num = ifp + 7;
                            }


                    } else { // south polar cap

                            ip = npix - ipring1 + 1;
                            hip = ip / 2.0;
                            fihip = Math.floor(hip);
                            irs = (long)( Math.sqrt(hip - Math.sqrt(fihip))) + 1;
                            iphi = 4 * irs + 1 - (ip - 2 * irs * (irs - 1));
                            kshift = 0;
                            nr = irs;
                            irn = nl4 - irs;
                            face_num = (iphi - 1) / irs + 8; // in [8,11]


                    }
                    // finds the (x,y) on the face


    //
                    irt = irn - jrll[(int) (face_num + 1)] * nside + 1; // in [-nside+1,0]
                    ipt = 2 * iphi - jpll[(int) (face_num + 1)] * nr - kshift - 1; // in [-nside+1,
                    // nside-1]
    //
                    if (ipt >= nl2){
                            ipt = ipt - 8*nside; // for the face #4
                    }
                    ix = (ipt - irt) / 2;
                    iy = -(ipt + irt) / 2;

                    ix_low = (long) PixToolsUtils.MODULO(ix, xmax);
                    ix_hi = ix / xmax;
                    iy_low = (long) PixToolsUtils.MODULO(iy, xmax);
                    iy_hi = iy / xmax;

              //

                    ipf = (x2pix[(int) (ix_hi + 1)] + y2pix[(int) (iy_hi + 1)]) * xmax * xmax
                                    + (x2pix[(int) (ix_low + 1)] + y2pix[(int) (iy_low + 1)]); // in [0, nside**2 -1]
                    ipnest = ipf + face_num * nside * nside; // in [0, 12*nside**2 -1]

                    return ipnest;

            }

    /**
     * converts from NESTED to RING pixel numbering
     *
     * @param nside
     *            long resolution
     * @param ipnest
     *            long NEST pixel number
     * @return ipring  long RING pixel number
     * @throws IllegalArgumentException
     */
    public static long nest2ring(long nside, long ipnest) {
                    long res = 0;
                    long npix, npface, face_num, ncap, n_before, ipf, ip_low, ip_trunc;
                    long ip_med, ip_hi, ix, iy, jrt, jr, nr, jpt, jp, kshift, nl4;
    //		long[] ixiy = { 0, 0 };
                    String SID = "nest2ring:";
                    //
                    if ((nside < 1) || (nside > PixTools.ns_max())) {
                            throw new IllegalArgumentException(SID + " nside should be power of 2 >0 and < ns_max");
                    }
                    npix = 12 * nside * nside;
                    if ((ipnest < 0) || (ipnest > npix - 1)) {
                            throw new IllegalArgumentException(SID + " ipnest out of range [0,npix-1]");
                    }
                    if (pix2x[pixmax-1] <= 0)
                            mk_pix2xy();
                    ncap = 2 * nside * (nside - 1); // number of points in the North polar
                    // cap
                    nl4 = 4 * nside;
                    // finds the face and the number in the face
                    npface = nside * nside;

                    face_num = ipnest / npface; // face number in [0,11]
                    if (ipnest >= npface) {
                            ipf = (long) PixToolsUtils.MODULO(ipnest, npface); // pixel number in the face
                    } else {
                            ipf = ipnest;
                    }

                    // finds the x,y on the face
                    //  from the pixel number
                    ip_low = (long) PixToolsUtils.MODULO(ipf, pixmax); // last 15 bits
                    if (ip_low < 0)
                            ip_low = -ip_low;

                    ip_trunc = ipf / pixmax; // truncate last 15 bits
                    ip_med = (long) PixToolsUtils.MODULO(ip_trunc, pixmax); // next 15 bits
                    if (ip_med < 0)
                            ip_med = -ip_med;
                    ip_hi = ip_trunc / pixmax; // high 15 bits

                    ix = pixmax * pix2x[(int) ip_hi] + xmid * pix2x[(int) ip_med] + pix2x[(int) ip_low];
                    iy = pixmax * pix2y[(int) ip_hi] + xmid * pix2y[(int) ip_med] + pix2y[(int) ip_low];

                    // transform this in (horizontal, vertical) coordinates
                    jrt = ix + iy; // vertical in [0,2*(nside -1)]
                    jpt = ix - iy; // horizontal in [-nside+1, nside - 1]
                    // calculate the z coordinate on the sphere
                    jr = jrll[(int) (face_num + 1)] * nside - jrt - 1; // ring number in [1,4*nside
                    // -1]
                    nr = nside; // equatorial region (the most frequent)
                    n_before = ncap + nl4 * (jr - nside);
                    kshift = (long) PixToolsUtils.MODULO(jr - nside, 2);
                    if (jr < nside) { // north pole region
                            nr = jr;
                            n_before = 2 * nr * (nr - 1);
                            kshift = 0;
                    } else if (jr > 3 * nside) { // south pole region
                            nr = nl4 - jr;
                            n_before = npix - 2 * (nr + 1) * nr;
                            kshift = 0;
                    }
                    // computes the phi coordinate on the sphere in [0,2pi]
                    jp = (jpll[(int) (face_num + 1)] * nr + jpt + 1 + kshift) / 2; // 'phi' number
                    // in ring
                    // [1,4*nr]
                    if (jp > nl4)
                            jp -= nl4;
                    if (jp < 1)
                            jp += nl4;
                    res = n_before + jp - 1; // in [0, npix-1]
                    return res;
            }

	/**
	 * creates an array of pixel numbers pix2x from x and y coordinates in the
	 * face. Suppose NESTED scheme of pixel ordering Bits corresponding to x and
	 * y are interleaved in the pixel number in even and odd bits.
	 */
	protected static void mk_pix2xy() {
		long kpix, jpix, ix, iy, ip, id;
//		boolean flag = true;
		for (kpix = 0; kpix <= pixmax; kpix++) { // loop on pixel numbers
			jpix = kpix;
			ix = 0;
			iy = 0;
			ip = 1; // bit position in x and y

			while (jpix != 0) { // go through all the bits

				id = (long) PixToolsUtils.MODULO(jpix, 2); // bit value (in kpix), goes in x
				jpix /= 2;
				ix += id * ip;

				id = (long) PixToolsUtils.MODULO(jpix, 2); // bit value, goes in iy
				jpix /= 2;
				iy += id * ip;

				ip *= 2; // next bit (in x and y )
			}

			pix2x[(int) kpix] = ix; // in [0,pixmax]
			pix2y[(int) kpix] = iy; // in [0,pixmax]


		}
    //    System.out.println(kpix);
	}

    /**
     * fills arrays x2pix and y2pix giving the number of the pixel laying in
     * (x,y). x and y are in [1,512] the pixel number is in [0, 512**2 -1]
     *
     * if i-1 = sum_p=0 b_p*2^p then ix = sum+p=0 b_p*4^p iy = 2*ix ix + iy in
     * [0,512**2 -1]
     *
     */
    protected static void mk_xy2pix() {
            long k, ip, id;

            for (int i = 1; i <= xmax; i++) {
                    long j = i - 1;
                    k = 0;
                    ip = 1;
                    while (j != 0) {
                            id = (long) PixToolsUtils.MODULO(j, 2);
                            j /= 2;
                            k += ip * id;
                            ip *= 4;
                    }
                    x2pix[i] = k;
                    y2pix[i] = 2 * k;

            }

    }


}
