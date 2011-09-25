package skyview.survey;

import java.io.EOFException;
import java.io.InputStream;

/**
 * HDecompressor. Uncompress astronomical images compressed
 * using the H-compression algorithm.
 * Examples :
 *
 * <PRE>
 *          DataInputStream dis = new DataInputStream( new FileInputStream(arg[0]));
 *          byte [] pixels=Hdecomp.decomp( (InputStream)dis );
 * </PRE>
 * where the user needs to translate the byte stream back into a series of shorts.
 * 
 * <PRE>
 *          DataInputStream dis = new DataInputStream( new FileInputStream(arg[0]));
 *          Hdecomp         hd  = new Hdecomp(dis);
 *          hd.decomp();
 *          int[] pixels        =  hd.getImage();
 *          int   nx            =  hd.getNx();
 *          int   ny            =  hd.getNy();
 * </PRE>
 * 
 *
 * @author Based on Hdecomp by Pierre Fernique [CDS] which in turn
 *         was based on hdecomp package (C language) by R. White - 1991
 *         Modified by Tom McGlynn[NASA] 2003
 * @Copyright (c) 1993 Association of Universities for Research in Astronomy
 */
public class HDecompressor {
    
  
    /** The size of buffer used of I/O */
    private static final int 	        SIZEBUF	=	8192;
    /** Natural log of 2 */
    private static final double 	log2 	= 	Math.log(2.);
    /** Special code that encoded files should begin with */
    private static final int[] 	        code_magic = 	{ 0xDD, 0x99 };	
    
    /** Internal I/O buffer */
    private byte[] 	                buf 	= 	new byte[SIZEBUF];
    
    /** Pointer within the buffer */
    private int 		        ptBuf	=	0;
   
    /** Current used size of buffer */
    private int 		        maxBuf	=	0;
    
    /** Input stream from which to get compressed data */
    private InputStream 	        dis;
   
   
    /** Size of the image */
    private int 		        nx,ny;
    
    /** Number of pixels in the image (nx*ny) */
    private int 		        nel;
    
    /** Hcompress scale */
    private int 		        scale;
    
    /** Buffer for image pixels. */
    private int 		        a[];
   
    /** Bits waiting to be input */
    private int 		        buffer;
    
    /** Bits still in buffer */
    private int 		        bits_to_go;
    
    private int[]  	                nbitplanes = 	new int[3];
    
    /** Internal array used in qtree_decode */
    private byte[] 	                scratch;
   
    /** Internal array used in yunshuffle. Allocated once to
     *  save on multiple allocations
     */
    private int[]	                tmp;
    
    /** Internal array used in yunshuffle. */
    private boolean[]	                flag;

    /** Create a bare decompressor object.
     */
    public HDecompressor() {
    }
    
    /** Create a decompressor associating it with a given input stream.
     *  @param dis  The input stream from which the compressed data is derived.
     */
    public HDecompressor(InputStream dis) {
        this();
        setInputStream(dis);
    }
   
    /** Set the input stream for an Hdecomp object.
     *  @param dis  The input stream from which the compressed data is derived.
     */
    public void setInputStream(InputStream dis) {
        this.dis = dis;
    }
       
    /**
     * Hdecompress static method.
     * @param fdis The input stream from which compressed data
     *              is to be extracted.  It should begin with the
     *              magic characters [0xDD, 0x99].  If the compressed data
     *              is preceded by a FITS header it is the responsibility
     *              of the program to skip past the header before
     *              calling this routine.
     * 
     * @return byte[] The uncompressed Fits image.  Note that
     *              this image is actually an array of shorts and the
     *              calling program should combine pairs of bytes to
     *              get the value for each pixel.
     */
    public static int[] decompress(InputStream fdis) throws Exception {
       
        HDecompressor hd = new HDecompressor(fdis);
        hd.decompress();
	return hd.getImage();
    }
    
    /** Decompress the input stream.  The result is
     *  left in the a[] array and can be accessed using
     *  the various get methods.
     */
    public void decompress() throws Exception {
	
        decode();			// Launch decoding
        undigitize();		// Un-Digitize
      
        if (tmp == null || tmp.length < Math.max(nx,ny)) {
            tmp   = new int[Math.max(nx,ny)];
            flag  = new boolean[Math.max(nx,ny)];
        }
       
        hinv();			// Inverse H-transform
    }
    
    /** Get the X-dimension of the decompressed image.
     *  @return The number of columns in the image.
     */
    public int getNx() {
        return nx;
    }
    
    /** Get the Y-dimension of the decompressed image.
     *  @return The number of rows in the image.
     */
    public int getNy() {
        return ny;
    }
    
    /** Get decompressed image as a one-d array of ints.
     *  Note that this returns a pointer to the internal array.
     *  If another image is compressed it may overwrite the
     *  current image in place.  The caller should copy
     *  the array if multiple files are being decompressed
     *  using the same Hdecomp object.
     *  @return The decompressed image as a 1-D int array.
     */
    public int[] getImage() {
        return a;
    }
    
    boolean debug = true;
    /** Input buffering.
     * @return the next byte */
    private int getc() throws Exception {
       
        while( ptBuf == maxBuf ) {
	    
            ptBuf=0;
            if( (maxBuf=dis.read(buf,0,buf.length)) == -1 ) {
		throw new EOFException();
	    }
        }
	if (debug) {
	    debug = false;
        }
        return (int)buf[ptBuf++] & 0xFF;
    }
   
    /** Input buffering.
     * @return the next int */
    private int getint() throws Exception {
        return (getc()<<24) | (getc()<<16) | (getc()<<8) | getc();
    }
   
    /** Initialize bit input */
    private void start_inputing_bits() {
        bits_to_go = 0;
    }

    /** Input a bit */
    private int input_bit() throws Exception {

        if ( bits_to_go == 0) {      /* Read the next byte if no     */
            buffer = getc();    /* bits are left in buffer      */
            bits_to_go = 8;
        }

        /* Return the next bit */      
        return((buffer>>(--bits_to_go)) & 1);
    }


    /** Input n bits (but not more than 1 byte) */
    private int input_nbits(int n) throws Exception {

        if (bits_to_go < n) {

            /* need another byte's worth of bits */
            buffer <<= 8;
            buffer |= getc();
            bits_to_go += 8;
        }

        /*  now pick off the first n bits */
        bits_to_go -= n;
        return( (buffer>>bits_to_go) & ((1<<n)-1) );
    }

    /**
     * Huffman decoding for fixed codes
     *
     * Coded values range from 0-15
     *
     * Huffman code values (hex):
     *<pre>
     *	3e, 00, 01, 08, 02, 09, 1a, 1b,
     *	03, 1c, 0a, 1d, 0b, 1e, 3f, 0c
     *
     * and number of bits in each code:
     *
     *	6,  3,  3,  4,  3,  4,  5,  5,
     *	3,  5,  4,  5,  4,  5,  6,  4
     *</pre>
     * @return The appropriate Huffman code.
     */
    private int input_huffman() throws Exception {
        int c;

        /* get first 3 bits to start */
        c = input_nbits(3);
        if (c < 4) {
            /* this is all we need
             * return 1,2,4,8 for c=0,1,2,3 */
            return(1<<c);
        }

        /* get the next bit */
        c = input_bit() | (c<<1);
        if (c < 13) {
            /* OK, 4 bits is enough */
            switch (c) {
              case  8 : return(3);
              case  9 : return(5);
              case 10 : return(10);
              case 11 : return(12);
              case 12 : return(15);
            }
        }

        /* get yet another bit */
        c = input_bit() | (c<<1);
        if (c < 31) {
            /* OK, 5 bits is enough */
            switch (c) {
              case 26 : return(6);
              case 27 : return(7);
              case 28 : return(9);
              case 29 : return(11);
              case 30 : return(13);
            }
        }

        /* need the 6th bit */
        c = input_bit() | (c<<1);
        if (c == 62) return(0);
        else return(14);
    }

    /**
     * Copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding
     * each value to 2x2 pixels and inserting into bitplane BIT of B.
     * A,B may NOT be same array (it wouldn't make sense to be inserting
     * bits into the same array anyway.)
     * @param n declared y dimension of b
     */
    private void qtree_bitins(byte d[],int nx,int ny,
                              int off,int n,int bit) {
        int i, j;
        int s00, s10;
        int nxN=nx-1;
        int nyN=ny-1;
        int c;
        int dc;

        /* expand each 2x2 block */
        c=0;				/* k   is index of a[i/2,j/2] */
        for (i = 0; i<nxN; i += 2) {
            s00 = off+n*i;			/* s00 is index of a[i,j] */
            s10 = s00+n;			/* s10 is index of a[i+1,j] */
            for (j = 0; j<nyN; j += 2) {
                dc = d[c];
                a[s10+1] |= ( dc     & 1) << bit;
                a[s10  ] |= ((dc>>1) & 1) << bit;
                a[s00+1] |= ((dc>>2) & 1) << bit;
                a[s00  ] |= ((dc>>3) & 1) << bit;
                s00 += 2;
                s10 += 2;
                c++;
            }
            if (j < ny) {
                /* row size is odd, do last element in row
                 * s00+1, s10+1 are off edge */
                dc = d[c];
                a[s10  ] |= ((dc>>1) & 1) << bit;
                a[s00  ] |= ((dc>>3) & 1) << bit;
                c++;
            }
        }
        if (i < nx) {
            /* column size is odd, do last row
             * s10, s10+1 are off edge */
            s00 = off+n*i;
            for (j = 0; j<nyN; j += 2) {
                dc = d[c];
                a[s00+1] |= ((dc>>2) & 1) << bit;
                a[s00  ] |= ((dc>>3) & 1) << bit;
                s00 += 2;
                c++;
            }
            if (j < ny) {
                /* both row and column size are odd, do corner element
                 * s00+1, s10, s10+1 are off edge */
                a[s00  ] |= ((d[c]>>3) & 1) << bit;
                c++;
            }
        }
    }

    private void read_bdirect(int off,int n,int nqx,int nqy,
			      byte scratch[],int bit) throws Exception {
        int i;
        int j=((nqx+1)/2) * ((nqy+1)/2);
      
        /* read bit image packed 4 pixels/nybble */
        for (i = 0; i < j; i++) {
            scratch[i] = (byte)(input_nbits(4));
        }

        /* insert in bitplane BIT of image A */
        qtree_bitins(scratch,nqx,nqy,off,n,bit);
    }

    /**
     * copy 4-bit values from a[(nx+1)/2,(ny+1)/2] to b[nx,ny], expanding
     * each value to 2x2 pixels
     * a,b may be same array
     * @param n declared y dimension of b
     */
    private void qtree_copy(byte d[],int nx,int ny,byte b[],int n) {
        int i, j, k, nx2, ny2;
        int s00, s10;
        int nxN=nx-1;
        int nyN=ny-1;
        int bs00;

        /* first copy 4-bit values to b
         * start at end in case a,b are same array */
        nx2 = (nx+1)/2;
        ny2 = (ny+1)/2;
        k   = ny2*(nx2-1)+ny2-1;  		/* k   is index of a[i,j] */
	
        for (i = nx2-1; i >= 0; i--) {
            s00 = (n*i+ny2-1)<<1;		/* s00 is index of b[2*i,2*j] */
            for (j = ny2-1; j >= 0; j--) {
                b[s00] = d[k--];
                s00 -= 2;
            }
        }

        /* now expand each 2x2 block */
        for (i = 0; i<nxN; i += 2) {
            s00 = n*i;		/* s00 is index of b[i,j] */
            s10 = s00+n;		/* s10 is index of b[i+1,j] */
            for (j = 0; j<nyN; j += 2) {
                bs00=b[s00];
                b[s10+1] = (byte)( bs00     & 1);
                b[s10  ] = (byte)((bs00>>1) & 1);
                b[s00+1] = (byte)((bs00>>2) & 1);
                b[s00  ] = (byte)((bs00>>3) & 1);
                s00 += 2;
                s10 += 2;
            }
            if (j < ny) {
                /* row size is odd, do last element in row
                 * s00+1, s10+1 are off edge */
                bs00=b[s00];
                b[s10  ] = (byte)((bs00>>1) & 1);
                b[s00  ] = (byte)((bs00>>3) & 1);
            }
        }
        if (i < nx) {
            /* column size is odd, do last row
             * s10, s10+1 are off edge */
            s00 = n*i;
            for (j = 0; j<nyN; j += 2) {
                bs00=b[s00];
                b[s00+1] = (byte)((bs00>>2) & 1);
                b[s00  ] = (byte)((bs00>>3) & 1);
                s00 += 2;
            }
            if (j < ny) {
                /* both row and column size are odd, do corner element
                 * s00+1, s10, s10+1 are off edge */
                b[s00  ] = (byte)((b[s00]>>3) & 1);
            }
        }
    }

    /**
     * do one quadtree expansion step on array a[(nqx+1)/2,(nqy+1)/2]
     * results put into b[nqx,nqy] (which may be the same as a)
     */
    private void qtree_expand(byte d[],int nx,int ny, byte b[])
                 throws Exception {
        int i;

        /* first copy a to b, expanding each 4-bit value */
        qtree_copy(d,nx,ny,b,ny);

        /* now read new 4-bit values into b for each non-zero element */
        for (i = nx*ny-1; i >= 0; i--) {
           if( b[i]!= 0 ) b[i] = (byte) input_huffman();
        }
    }
   
    /**
     * @param n length of full row in a
     * @param nqx partial length of row to decode
     * @param nqy partial length of column (<=n)
     * @param nbitplanes number of bitplanes to decode 
     */
    private void qtree_decode(int off,int n,int nqx,int nqy,int nbitplanes)
                 throws Exception {
        int log2n, k, bit, b, nqmax;
        int nx,ny,nfx,nfy,c;
        int nqx2, nqy2;

        /* log2n is log2 of max(nqx,nqy) rounded up to next power of 2 */
        nqmax = (nqx>nqy) ? nqx : nqy;
        log2n = (int)( Math.log(nqmax)/log2+0.5 );
        if (nqmax > (1<<log2n)) log2n += 1;

        /* allocate scratch array for working space */
        nqx2=(nqx+1)/2;
        nqy2=(nqy+1)/2;
	if (scratch == null || scratch.length != nqx2*nqy2) {
            scratch = new byte[nqx2 * nqy2];
	} else {
	    java.util.Arrays.fill(scratch, (byte)0);
	}

        /*
         * now decode each bit plane, starting at the top
         * A is assumed to be initialized to zero
         */
        for ( bit = nbitplanes-1; bit >= 0; bit--) {

            /* Was bitplane was quadtree-coded or written directly? */
            b = input_nbits(4);
            if( b == 0) {
                /* bit map was written directly */
                read_bdirect(off,n,nqx,nqy,scratch,bit);
            } else if (b != 0xf) {
                throw new Exception("qtree_decode: bad format code "+b);
            } else {

                /* bitmap was quadtree-coded, do log2n expansions
                 * read first code */
                scratch[0] = (byte) input_huffman();

                /* now do log2n expansions, reading codes from file as necessary */
                nx = 1;
                ny = 1;
                nfx = nqx;
                nfy = nqy;
		
                c = 1<<log2n;
                for (k = 1; k<log2n; k++) {
	            /* this somewhat cryptic code generates the sequence
	             * n[k-1] = (n[k]+1)/2 where n[log2n]=nqx or nqy */
	            c = c>>1;
	            nx = nx<<1;
	            ny = ny<<1;
	            if (nfx <= c) { nx -= 1; } else { nfx -= c; }
	            if (nfy <= c) { ny -= 1; } else { nfy -= c; }
	            qtree_expand(scratch,nx,ny,scratch); 
                }

                /* now copy last set of 4-bit codes to bitplane bit of array a */
                qtree_bitins(scratch,nqx,nqy,off,n,bit);
            }
        }
    }

    private void dodecode() throws Exception {


        int nx2 = (nx+1)/2;
        int ny2 = (ny+1)/2;


        /* Initialize bit input */
        start_inputing_bits();

        int ny_nx2 =  ny*nx2;
        int ny_div2 = ny/2;
        int nx_div2 = nx/2;

        /* Read bit planes for each quadrant */
        qtree_decode( 0,          ny, nx2,  ny2,  nbitplanes[0]);
        qtree_decode( ny2,        ny, nx2,  ny_div2, nbitplanes[1] );
        qtree_decode( ny_nx2,     ny, nx_div2, ny2,  nbitplanes[1] );
        qtree_decode( ny_nx2+ny2, ny, nx_div2, ny_div2, nbitplanes[2] );

        /* Make sure there is an EOF symbol (nybble=0) at end */
        if (input_nbits(4) != 0) {
            System.err.println("dodecode: bad bit plane values\n");
            throw new Exception("Error in dodecode decompression");
        }

        /* Now get the sign bits - Re-initialize bit input*/
        start_inputing_bits();
        int aa;
        for (int i=0; i<nel; i++) {
            if( (aa=a[i])!=0 && input_bit() != 0)
                a[i] = -aa;
        }

    }

    private void decode() throws Exception {
        int sumall;

        // Init the buffer mechanism
        ptBuf=maxBuf = 0;
      
        // Read magic number
        if( (getc())!=code_magic[0] || (getc())!=code_magic[1] ) {
            throw new Exception("Bad magic number");
        }
      
        // read size
        nx = getint();	
        ny = getint();
        nel= nx*ny;
      
        // read scale
        scale=getint();
      
        // allocation
        if (a == null || a.length != nel) {
             a = new int[nel];
        } else {
	     java.util.Arrays.fill(a,0);
        }
      
        // sum of all pixels
        sumall=getint();

        // # bits in quadrants
        nbitplanes[0]=getc();
        nbitplanes[1]=getc();
        nbitplanes[2]=getc();
      
        // Go
        dodecode();
        
        // at the end
        a[0]=sumall;
    }

    private void undigitize() {
        if (scale <= 1) return;
        for( int i=nel-1; i>=0; i-- ) a[i] *= scale;
    }


    /** Invert the H transform */
    private void hinv() {
	
        /*
         * log2n is log2 of max(nx,ny) rounded up to next power of 2
         */
        int nmax  = (nx>ny) ? nx : ny;
        int log2n = (int)(Math.log((double) nmax)/Math.log(2.0)+0.5);
	
        if ( nmax > (1<<log2n) ) {
            log2n += 1;
        }
        /*
         * do log2n expansions
         *
         * We're indexing a as a 2-D array with dimensions (nx,ny).
         */
        int nxtop = 1;
        int nytop = 1;
	
        int nxf = nx;
        int nyf = ny;
        int c = 1<<log2n;
	
        for (int k = log2n-1; k>0; k--) {
            /*
             * this somewhat cryptic code generates the sequence
             * ntop[k-1] = (ntop[k]+1)/2, where ntop[log2n] = n
             */
            c     >>= 1;
            nxtop <<= 1;
            nytop <<= 1;
	    
            if (nxf <= c) { 
		nxtop -= 1; 
	    } else { 
		nxf -= c; 
	    }
            if (nyf <= c) {
		nytop -= 1; 
	    } else { 
		nyf -= c; 
	    }
            /*
             * unshuffle in each dimension to interleave coefficients
             */
            xunshuffle(a,nxtop,nytop,ny);
            yunshuffle(a,nxtop,nytop,ny);
            for (int i = 0; i<nxtop-1; i += 2) {
                int pend = ny*i+nytop-1;
		
		int p00=0, p10=0;
		
		for (p00=ny*i, p10=ny*(i+1); p00<pend; p00 += 2, p10 += 2) {
		    
                    int h0 = a[p00];
                    int hx = a[p10];
		    int hy = a[p00+1];
                    int hc = a[p10+1];
		    
                    /*
                     * Divide sums by 2
                     */
                    int sum1 = h0+hx+1;
                    int sum2 = hy+hc;
                    a[p10+1] = (sum1 + sum2) >> 1;
                    a[p10  ] = (sum1 - sum2) >> 1;
                    sum1 = h0-hx+1;
                    sum2 = hy-hc;
                    a[p00+1] = (sum1 + sum2) >> 1;
                    a[p00  ] = (sum1 - sum2) >> 1;
			 
                }
                if (p00 == pend) {
                    /*
                     * do last element in row if row length is odd
                     * p00+1, p10+1 are off edge
                     */
                    int h0     = a[p00  ];
                    int hx     = a[p10  ];
                    a[p10] = (h0 + hx + 1) >> 1;
                    a[p00] = (h0 - hx + 1) >> 1;
                }
            }
            if ( nxtop%2 == 1) {
		int i = nxtop-1;
                /*
                 * do last row if column length is odd
                 * p10, p10+1 are off edge
                 */
                int pend = ny*i+nytop-1;
		int p00;
                for (p00 = ny*i; p00 < pend; p00 += 2) {
		    
		    
                    int h0 = a[p00  ];
                    int hy = a[p00+1];
                    a[p00+1] = (h0 + hy + 1) >> 1;
                    a[p00  ] = (h0 - hy + 1) >> 1;
		}
		    
                if (p00 == pend) {
                    /*
                     * do corner element if both row and column lengths are odd
                     * p00+1, p10, p10+1 are off edge
                     */
                    a[p00] = (a[p00] + 1) >> 1;
                }
            }
	}
	
        /*
         * Last pass (k=0) has some differences:
         *
         * Shift by 2 instead of 1
         *
         * Use explicit values for all variables to avoid unnecessary shifts etc:
         *
         *   N    bitN maskN prndN nrndN
         *   0     1    -1     0     0  (note nrnd0 != prnd0-1)
         *   1     2    -2     1     0
         *   2     4    -4     2     1
         */

        /*
         * Check nxtop=nx, nytop=ny
         */
        c     >>= 1;
        nxtop <<= 1;
        nytop <<= 1;
	
        if (nxf <= c) { 
	    nxtop -= 1; 
	} else { 
	    nxf -= c; 
	}
	
        if (nyf <= c) { 
	    nytop -= 1; 
	} else { 
	    nyf -= c; 
	}
	
        if (nxtop != nx || nytop != ny) {
            System.err.println(
               "hinv: error, final image size is "+nxtop+" x "+ nytop +
	       " not "+nx+" x "+ny);
        }
	
        /*
         * unshuffle in each dimension to interleave coefficients
         */
        xunshuffle(a,nx,ny,ny);
        yunshuffle(a,nx,ny,ny);
	
        for (int i = 0; i<nx-1; i += 2) {
	    
            int pend = ny*i+ny-1;
	    int p00 = 0, p10 = 0;
            for (p00 = ny*i, p10 = p00+ny;
                 p00 < pend; 
		 p00 += 2, p10 += 2) {
                int h0 = a[p00  ];
                int hx = a[p10  ];
                int hy = a[p00+1];
                int hc = a[p10+1];
                /*
                 * Divide sums by 4
                 */
                int sum1 = h0+hx+2;
                int sum2 = hy+hc;
                a[p10+1] = (sum1 + sum2) >> 2;
                a[p10  ] = (sum1 - sum2) >> 2;
                sum1 = h0-hx+2;
                sum2 = hy-hc;
                a[p00+1] = (sum1 + sum2) >> 2;
                a[p00  ] = (sum1 - sum2) >> 2;
            }
            if (p00 == pend) {
                /*
                 * Do last element in row if row length is odd
                 * p00+1, p10+1 are off edge
                 */
                int h0 = a[p00];
                int hx = a[p10];
                a[p10] = (h0 + hx + 2) >> 2;
                a[p00] = (h0 - hx + 2) >> 2;
            }
        }
        if (nx%2 == 1) {
	    int i = nx-1;
            /*
             * Do last row if column length is odd
             * p10, p10+1 are off edge
             */
            int pend = ny*i+ny-1;
	    int p00;
            for (p00 = ny*i; p00 < pend; p00 += 2) {
                int h0   = a[p00  ];
                int hy   = a[p00+1];
                a[p00+1] = (h0 + hy + 2) >> 2;
                a[p00  ] = (h0 - hy + 2) >> 2;
            }
            if (p00==pend) {
                /*
                 * Do corner element if both row and column lengths are odd
                 * p00+1, p10, p10+1 are off edge
                 */
                a[p00] = a[p00 + 2] >> 2;
            }
        }
    }
    
    private void xunshuffle(int[] a, int nx, int ny, int nydim) {
	
        int nhalf = (ny+1)>>1;
        for (int j = 0; j<nx; j++) {
            /*
             * copy 2nd half of array to tmp
             */
            System.arraycopy(a, j*nydim+nhalf, tmp, 0, ny-nhalf);
            /*
             * distribute 1st half of array to even elements
             */
            int pend = j*nydim;
            for (int p2 = j*nydim+nhalf-1, p1 = j*nydim + ((nhalf-1)<<1);
                p2 >= pend; 
		p1 -= 2, p2 -= 1) {
                a[p1] = a[p2];
            }
            /*
             * now distribute 2nd half of array (in tmp) to odd elements
             */
            pend = j*nydim+ny;
            for (int pt = 0, p1 = j*nydim+1; p1<pend; p1 += 2, pt += 1) {
                a[p1] = tmp[pt];
            }
        }
    }

    private void yunshuffle(int[] a, int nx,int ny,int nydim) {

        /*
         * initialize flag array telling whether row is done
         */
        for (int j=0; j<nx; j++) flag[j] = true;

        int oddoffset = (nx+1)/2;
        /*
         * shuffle each row to appropriate location
         * row 0 is already in right location
         */
        int k = 0;
	
        for (int j=1; j<nx; j++) {
            if (flag[j]) {
                flag[j] = false;
                /*
                 * where does this row belong?
                 */
                if (j >= oddoffset) {
                    /* odd row */
                    k = ((j-oddoffset)<<1) + 1;
                } else {
                    /* even row */
                    k = j<<1;
                }
                if (j != k) {
                    /*
                     * copy the row
                     */
                    System.arraycopy(a, nydim*j, tmp, 0, ny);
                    /*
                     * keep shuffling until we reach a row that is done
                     */
                    while (flag[k]) {
                        flag[k] = false;
                        /*
                         * do the exchange
                         */
                        for (int p = nydim*k, pt=0;
                            p < nydim*k+ny; p++, pt++) {
                            int tt = a[p];
                            a[p] = tmp[pt];
                            tmp[pt] = tt;
			     
                        }
                        if (k >= oddoffset) {
                            k = ((k-oddoffset)<<1) + 1;
                        } else {
                            k <<= 1;
                        }
                    }
                    /*
                     * copy the last row into place
                     * this should always end up with j=k
                     */
		    System.arraycopy(tmp, 0, a, nydim*k, ny);
                    if (j != k) {
		        System.err.println(
                           "error: yunshuffle failed!\nj="+j+" k="+k);
                    }
		}
	    }
	}

    }
}
