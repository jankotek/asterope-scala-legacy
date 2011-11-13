/*
 * This file is part of JPARSEC library.
 * 
 * (C) Copyright 2006-2009 by T. Alonso Albi - OAN (Spain).
 *  
 * Project Info:  http://conga.oan.es/~alonso/jparsec/jparsec.html
 * 
 * JPARSEC library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPARSEC library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.asterope.util;

import org.asterope.ephem.EphemUtils;

import java.io.Serializable;


/**
 * A support class for complex numbers.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class Complex implements Serializable {

	private static final long serialVersionUID = -1158712514846992359L;
	
	/**
	 * Real part.
	 */
	public double real;
	/**
	 * Imaginary part.
	 */
	public double imaginary;

	/**
	 * Default constructor.
	 */
	public Complex ()
	{
		this.real = this.imaginary = 0.0;
	}

	/**
	 * Explicit constructor.
	 * @param r Real.
	 * @param i Imaginary.
	 */
	public Complex (double r, double i)
	{
		this.real = r;
		this.imaginary = i;
	}

	/**
	 * Add operation.
	 * @param a A complex number.
	 * @return The sum of both.
	 */
	public Complex add(Complex a)
	{
		Complex c = new Complex();
		c.real=a.real+this.real;
		c.imaginary=a.imaginary+this.imaginary;
		return c;
	}

	/**
	 * Substract a complex to this instance.
	 * @param a A complex number.
	 * @return this-b.
	 */
	public Complex substract( Complex a)
	{
		Complex c = new Complex();
		c.real=this.real-a.real;
		c.imaginary=this.imaginary-a.imaginary;
		return c;
	}

	/**
	 * Product operation.
	 * @param a A complex number.
	 * @return this*b.
	 */
	public Complex multiply(Complex a)
	{
		Complex c = new Complex();
		c.real=this.real*a.real-this.imaginary*a.imaginary;
		c.imaginary=this.imaginary*a.real+this.real*a.imaginary;
		return c;
	}

	/**
	 * Obtains the conjugate.
	 * @return The conjugate.
	 */
	public Complex conjugate()
	{
		Complex c = new Complex();
		c.real=this.real;
		c.imaginary = -this.imaginary;
		return c;
	}

	/**
	 * Division operation.
	 * @param b A complex number.
	 * @return this/b.
	 */
	public Complex div(Complex b)
	{
		Complex c = new Complex();
		double r,den;
		if (Math.abs(b.real) >= Math.abs(b.imaginary)) {
			r=b.imaginary/b.real;
			den=b.real+r*b.imaginary;
			c.real=(this.real+r*this.imaginary)/den;
			c.imaginary=(this.imaginary-r*this.real)/den;
		} else {
			r=b.real/b.imaginary;
			den=b.imaginary+r*b.real;
			c.real=(this.real*r+this.imaginary)/den;
			c.imaginary=(this.imaginary*r-this.real)/den;
		}
		return c;
	}

	/**
	 * Absolute value.
	 * @return The absolute value.
	 */
	public double abs()
	{
		double x,y,ans,temp;
		x=Math.abs(this.real);
		y=Math.abs(this.imaginary);
		if (x == 0.0)
			ans=y;
		else if (y == 0.0)
			ans=x;
		else if (x > y) {
			temp=y/x;
			ans=x*Math.sqrt(1.0f+temp*temp);
		} else {
			temp=x/y;
			ans=y*Math.sqrt(1.0f+temp*temp);
		}
		return ans;
	}

	/**
	 * Obtains the root square.
	 * @return The root square.
	 */
	public Complex sqrt()
	{
		Complex c = new Complex();
		double x,y,w,r;
		if ((this.real == 0.0) && (this.imaginary == 0.0)) {
			c.real=0.0;
			c.imaginary=0.0;
			return c;
		} else {
			x=Math.abs(this.real);
			y=Math.abs(this.imaginary);
			if (x >= y) {
				r=y/x;
				w=Math.sqrt(x)*Math.sqrt(0.5*(1.0+Math.sqrt(1.0+r*r)));
			} else {
				r=x/y;
				w=Math.sqrt(y)*Math.sqrt(0.5*(r+Math.sqrt(1.0+r*r)));
			}
			if (this.real >= 0.0) {
				c.real=w;
				c.imaginary=this.imaginary/(2.0*w);
			} else {
				c.imaginary=(this.imaginary >= 0) ? w : -w;
				c.real=this.imaginary/(2.0*c.imaginary);
			}
			return c;
		}
	}

	/**
	 * Multiplies a complex by a scalar.
	 * @param x A number.
	 * @return this*x.
	 */
	public Complex multiply(double x)
	{
		Complex c = new Complex();
		c.real=x*this.real;
		c.imaginary=x*this.imaginary;
		return c;
	}
	
	/**
	 * Returns the exponential function of certain Complex.
	 * @return Exp(this) as a new Complex.
	 */
	public Complex exponential()
	{
		double ang = EphemUtils.normalizeRadians(this.imaginary);
		return new Complex(Math.exp(this.real) * Math.cos(ang), 
				Math.exp(this.real) * Math.sin(ang));
	}

    /**
	    * Returns the <i>principal</i> angle of a <tt>Complex</tt> number, in
	    * radians, measured counter-clockwise from the real axis.  (Think of the
	    * reals as the x-axis, and the imaginaries as the y-axis.)
	    *
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>arg(z)</i>, the others are of
	    * the form:
	    * <p>
	    * <pre>
	    *     <b>A</b> + 2*k*<b>PI</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * <tt>arg()</tt> always returns a <tt>double</tt> between
	    * -<tt><b>PI</b></tt> and +<tt><b>PI</b></tt>.
	    * <p>
	    * <i><b>Note:</b><ul> 2*<tt><b>PI</b></tt> radians is the same as 360 degrees.
	    * </ul></i>
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> There are no restrictions: the
	    * class defines arg(0) to be 0
	    * </ul></i>
	    * <p>
	    * @return                  Principal angle (in radians)
	    **/
	    public double arg () {
	        return  Math.atan2(imaginary, real);
	    }//end arg()
	    
    /**
	    * Returns the sine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     sin(z)  =  ( exp(<i><b>i</b></i>*z) - exp(-<i><b>i</b></i>*z) ) / (2*<i><b>i</b></i>)
	    * </pre>
	    * <p>
	    * @return                  The <tt>Complex</tt> sine
	    * <p>
	    * @see                     Complex#asin()
	    * @see                     Complex#sinh()
	    * @see                     Complex#cosec()
	    * @see                     Complex#cos()
	    * @see                     Complex#tan()
	    **/
	    public Complex sin () {
	        Complex result;
	            //  sin(z)  =  ( exp(i*z) - exp(-i*z) ) / (2*i)

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _1:      _1.sub(_2) ...
	            _re1 = _re1 - _re2;                                                // !!!
	            _im1 = _im1 - _im2;                                                // !!!

	            // result:  _1.div(2*i) ...
	            result =  new Complex( 0.5*_im1, -0.5*_re1 );
	            // result =  new Complex(_re1, _im1);
	            // div(result, 0.0, 2.0);


	        return  result;
	    }//end sin()



	    /**
	    * Returns the cosine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     cos(z)  =  ( exp(<i><b>i</b></i>*z) + exp(-<i><b>i</b></i>*z) ) / 2
	    * </pre>
	    * <p>
	    * @return                  The <tt>Complex</tt> cosine
	    * <p>
	    * @see                     Complex#acos()
	    * @see                     Complex#cosh()
	    * @see                     Complex#sec()
	    * @see                     Complex#sin()
	    * @see                     Complex#tan()
	    **/
	    public Complex cos () {
	        Complex result;
	            //  cos(z)  =  ( exp(i*z) + exp(-i*z) ) / 2

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _1:      _1.add(_2) ...
	            _re1 = _re1 + _re2;                                                // !!!
	            _im1 = _im1 + _im2;                                                // !!!

	            // result:  _1.scale(0.5) ...
	            result =  new Complex( 0.5 * _re1, 0.5 * _im1 );
	        return  result;
	    }//end cos()



	    /**
	    * Returns the tangent of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     tan(z)  =  sin(z) / cos(z)
	    * </pre>
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> tan(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
	    * where k is any integer
	    * </ul></i>
	    * <p>
	    * @return                  The <tt>Complex</tt> tangent
	    * <p>
	    * @see                     Complex#atan()
	    * @see                     Complex#tanh()
	    * @see                     Complex#cot()
	    * @see                     Complex#sin()
	    * @see                     Complex#cos()
	    **/
	    public Complex tan () {
	        Complex result;
	            //  tan(z)  =  sin(z) / cos(z)

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;
	            double _re3, _im3;

	            double cs_re, cs_im;

	            // sin() ...

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _3:      _1.sub(_2) ...
	            _re3 = _re1 - _re2;
	            _im3 = _im1 - _im2;

	            // result:  _3.div(2*i) ...
	            result =  new Complex( 0.5*_im3, -0.5*_re3 );
	            // result =  new Complex(_re3, _im3);
	            // div(result, 0.0, 2.0);

	            // cos() ...

	            // _3:      _1.add(_2) ...
	            _re3 = _re1 + _re2;
	            _im3 = _im1 + _im2;

	            // cs:      _3.scale(0.5) ...
	            cs_re =  0.5 * _re3;
	            cs_im =  0.5 * _im3;

	            // result:  result.div(cs) ...
	            result = result.div(new Complex(cs_re, cs_im));
	        return  result;
	    }//end tan()



	    /**
	    * Returns the cosecant of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     cosec(z)  =  1 / sin(z)
	    * </pre>
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> cosec(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
	    * where k is any integer
	    * </ul></i>
	    * <p>
	    * @return                  The <tt>Complex</tt> cosecant
	    * <p>
	    * @see                     Complex#sin()
	    * @see                     Complex#sec()
	    * @see                     Complex#cot()
	    **/
	    public Complex cosec () {
	        Complex result;
	            //  cosec(z)  =  1 / sin(z)

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _1:      _1.sub(_2) ...
	            _re1 = _re1 - _re2;                                                // !!!
	            _im1 = _im1 - _im2;                                                // !!!

	            // _result: _1.div(2*i) ...
	            result =  new Complex( 0.5*_im1, -0.5*_re1 );
	            // result =  new Complex(_re1, _im1);
	            // div(result, 0.0, 2.0);

	            // result:  one.div(_result) ...
	            inv(result);
	        return  result;
	    }//end cosec()



	    /**
	    * Returns the secant of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     sec(z)  =  1 / cos(z)
	    * </pre>
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> sec(z) is undefined whenever z = (k + 1/2) * <tt><b>PI</b></tt><br>
	    * where k is any integer
	    * </ul></i>
	    * <p>
	    * @return                  The <tt>Complex</tt> secant
	    * <p>
	    * @see                     Complex#cos()
	    * @see                     Complex#cosec()
	    * @see                     Complex#cot()
	    **/
	    public Complex sec () {
	        Complex result;
	            //  sec(z)  =  1 / cos(z)

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _1:      _1.add(_2) ...
	            _re1 = _re1 + _re2;
	            _im1 = _im1 + _im2;

	            // result: _1.scale(0.5) ...
	            result =  new Complex(0.5*_re1, 0.5*_im1);

	            // result:  one.div(result) ...
	            inv(result);
	        return  result;
	    }//end sec()



	    /**
	    * Returns the cotangent of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     cot(z)  =  1 / tan(z)
	    * </pre>
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> cot(z) is undefined whenever z = k * <tt><b>PI</b></tt><br>
	    * where k is any integer
	    * </ul></i>
	    * <p>
	    * @return                  The <tt>Complex</tt> cotangent
	    * <p>
	    * @see                     Complex#tan()
	    * @see                     Complex#cosec()
	    * @see                     Complex#sec()
	    **/
	    public Complex cot () {
	        Complex result;
	            //  cot(z)  =  1 / tan(z)  =  cos(z) / sin(z)

	            double scalar;
	            double iz_re, iz_im;
	            double _re1, _im1;
	            double _re2, _im2;
	            double _re3, _im3;

	            double sn_re, sn_im;

	            // cos() ...

	            // iz:      i.mul(z) ...
	            iz_re =  -imaginary;
	            iz_im =   real;

	            // _1:      iz.exp() ...
	            scalar =  Math.exp(iz_re);
	            _re1 =  scalar * Math.cos(iz_im);
	            _im1 =  scalar * Math.sin(iz_im);

	            // _2:      iz.neg().exp() ...
	            scalar =  Math.exp(-iz_re);
	            _re2 =  scalar * Math.cos(-iz_im);
	            _im2 =  scalar * Math.sin(-iz_im);

	            // _3:      _1.add(_2) ...
	            _re3 = _re1 + _re2;
	            _im3 = _im1 + _im2;

	            // result:  _3.scale(0.5) ...
	            result =  new Complex( 0.5*_re3, 0.5*_im3 );

	            // sin() ...

	            // _3:      _1.sub(_2) ...
	            _re3 = _re1 - _re2;
	            _im3 = _im1 - _im2;

	            // sn:      _3.div(2*i) ...
	            sn_re =    0.5 * _im3;                                             // !!!
	            sn_im =  - 0.5 * _re3;                                             // !!!

	            // result:  result.div(sn) ...
	            result = result.div(new Complex(sn_re, sn_im));
	        return  result;
	    }//end cot()



	    /**
	    * Returns the hyperbolic sine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     sinh(z)  =  ( exp(z) - exp(-z) ) / 2
	    * </pre>
	    * <p>
	    * @return                  The <tt>Complex</tt> hyperbolic sine
	    * <p>
	    * @see                     Complex#sin()
	    * @see                     Complex#asinh()
	    **/
	    public Complex sinh () {
	        Complex result;
	            //  sinh(z)  =  ( exp(z) - exp(-z) ) / 2

	            double scalar;
	            double _re1, _im1;
	            double _re2, _im2;

	            // _1:      z.exp() ...
	            scalar =  Math.exp(real);
	            _re1 =  scalar * Math.cos(imaginary);
	            _im1 =  scalar * Math.sin(imaginary);

	            // _2:      z.neg().exp() ...
	            scalar =  Math.exp(-real);
	            _re2 =  scalar * Math.cos(-imaginary);
	            _im2 =  scalar * Math.sin(-imaginary);

	            // _1:      _1.sub(_2) ...
	            _re1 = _re1 - _re2;                                                // !!!
	            _im1 = _im1 - _im2;                                                // !!!

	            // result:  _1.scale(0.5) ...
	            result =  new Complex( 0.5 * _re1, 0.5 * _im1 );
	        return  result;
	    }//end sinh()



	    /**
	    * Returns the hyperbolic cosine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     cosh(z)  =  ( exp(z) + exp(-z) ) / 2
	    * </pre>
	    * <p>
	    * @return                  The <tt>Complex</tt> hyperbolic cosine
	    * <p>
	    * @see                     Complex#cos()
	    * @see                     Complex#acosh()
	    **/
	    public Complex cosh () {
	        Complex result;
	            //  cosh(z)  =  ( exp(z) + exp(-z) ) / 2

	            double scalar;
	            double _re1, _im1;
	            double _re2, _im2;

	            // _1:      z.exp() ...
	            scalar =  Math.exp(real);
	            _re1 =  scalar * Math.cos(imaginary);
	            _im1 =  scalar * Math.sin(imaginary);

	            // _2:      z.neg().exp() ...
	            scalar =  Math.exp(-real);
	            _re2 =  scalar * Math.cos(-imaginary);
	            _im2 =  scalar * Math.sin(-imaginary);

	            // _1:  _1.add(_2) ...
	            _re1 = _re1 + _re2;                                                // !!!
	            _im1 = _im1 + _im2;                                                // !!!

	            // result:  _1.scale(0.5) ...
	            result =  new Complex( 0.5 * _re1, 0.5 * _im1 );
	        return  result;
	    }//end cosh()



	    /**
	    * Returns the hyperbolic tangent of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     tanh(z)  =  sinh(z) / cosh(z)
	    * </pre>
	    * <p>
	    * @return                  The <tt>Complex</tt> hyperbolic tangent
	    * <p>
	    * @see                     Complex#tan()
	    * @see                     Complex#atanh()
	    **/
	    public Complex  tanh () {
	        Complex result;
	            //  tanh(z)  =  sinh(z) / cosh(z)

	            double scalar;
	            double _re1, _im1;
	            double _re2, _im2;
	            double _re3, _im3;

	            double ch_re, ch_im;

	            // sinh() ...

	            // _1:      z.exp() ...
	            scalar =  Math.exp(real);
	            _re1 =  scalar * Math.cos(imaginary);
	            _im1 =  scalar * Math.sin(imaginary);

	            // _2:      z.neg().exp() ...
	            scalar =  Math.exp(-real);
	            _re2 =  scalar * Math.cos(-imaginary);
	            _im2 =  scalar * Math.sin(-imaginary);

	            // _3:      _1.sub(_2) ...
	            _re3 =  _re1 - _re2;
	            _im3 =  _im1 - _im2;

	            // result:  _3.scale(0.5) ...
	            result =  new Complex(0.5*_re3, 0.5*_im3);

	            // cosh() ...

	            // _3:      _1.add(_2) ...
	            _re3 =  _re1 + _re2;
	            _im3 =  _im1 + _im2;

	            // ch:      _3.scale(0.5) ...
	            ch_re =  0.5 * _re3;
	            ch_im =  0.5 * _im3;

	            // result:  result.div(ch) ...
	            result = result.div(new Complex(ch_re, ch_im));
	        return  result;
	    }//end tanh()



	    /**
	    * Returns the <i>principal</i> arc sine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     asin(z)  =  -<i><b>i</b></i> * log(<i><b>i</b></i>*z + sqrt(1 - z*z))
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>asin(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     k*<b>PI</b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * @return                  Principal <tt>Complex</tt> arc sine
	    * <p>
	    * @see                     Complex#sin()
	    * @see                     Complex#sinh()
	    **/
	    public Complex  asin () {
	        Complex result;
	            //  asin(z)  =  -i * log(i*z + sqrt(1 - z*z))

	            double _re1, _im1;

	            // _1:      one.sub(z.mul(z)) ...
	            _re1 =  1.0 - ( (real*real) - (imaginary*imaginary) );
	            _im1 =  0.0 - ( (real*imaginary) + (imaginary*real) );

	            // result:  _1.sqrt() ...
	            result =  new Complex(_re1, _im1);
	            result = result.sqrt();

	            // _1:      z.mul(i) ...
	            _re1 =  - imaginary;
	            _im1 =  + real;

	            // result:  _1.add(result) ...
	            result.real =  _re1 + result.real;
	            result.imaginary =  _im1 + result.imaginary;

	            // _1:      result.log() ...
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  i.neg().mul(_1) ...
	            result.real =    _im1;
	            result.imaginary =  - _re1;
	        return  result;
	    }//end asin()



	    /**
	    * Returns the <i>principal</i> arc cosine of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     acos(z)  =  -<i><b>i</b></i> * log( z + <i><b>i</b></i> * sqrt(1 - z*z) )
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>acos(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     2*k*<b>PI</b> +/- <b>A</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * @return                  Principal <tt>Complex</tt> arc cosine
	    * <p>
	    * @see                     Complex#cos()
	    * @see                     Complex#cosh()
	    **/
	    public Complex  acos () {
	        Complex result;
	            //  acos(z)  =  -i * log( z + i * sqrt(1 - z*z) )

	            double _re1, _im1;

	            // _1:      one.sub(z.mul(z)) ...
	            _re1 =  1.0 - ( (real*real) - (imaginary*imaginary) );
	            _im1 =  0.0 - ( (real*imaginary) + (imaginary*real) );

	            // result:  _1.sqrt() ...
	            result =  new Complex(_re1, _im1);
	            result = result.sqrt();

	            // _1:      i.mul(result) ...
	            _re1 =  - result.imaginary;
	            _im1 =  + result.real;

	            // result:  z.add(_1) ...
	            result.real =  real + _re1;
	            result.imaginary =  imaginary + _im1;

	            // _1:      result.log()
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  i.neg().mul(_1) ...
	            result.real =    _im1;
	            result.imaginary =  - _re1;
	        return  result;
	    }//end acos()



	    /**
	    * Returns the <i>principal</i> arc tangent of a <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     atan(z)  =  -<i><b>i</b></i>/2 * log( (<i><b>i</b></i>-z)/(<i><b>i</b></i>+z) )
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>atan(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     <b>A</b> + k*<b>PI</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> atan(z) is undefined for z = + <b>i</b> or z = - <b>i</b>
	    * </ul></i>
	    * <p>
	    * @return                  Principal <tt>Complex</tt> arc tangent
	    * <p>
	    * @see                     Complex#tan()
	    * @see                     Complex#tanh()
	    **/
	    public Complex atan () {
	        Complex result;
	            //  atan(z)  =  -i/2 * log( (i-z)/(i+z) )

	            double _re1, _im1;

	            // result:  i.sub(z) ...
	            result =  new Complex(- real, 1.0 - imaginary);

	            // _1:      i.add(z) ...
	            _re1 =  + real;
	            _im1 =  1.0 + imaginary;

	            // result:  result.div(_1) ...
	            result = result.div(new Complex( _re1, _im1));

	            // _1:      result.log() ...
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  half_i.neg().mul(_2) ...
	            result.real =   0.5*_im1;
	            result.imaginary =  -0.5*_re1;
	        return  result;
	    }//end atan()



	    /**
	    * Returns the <i>principal</i> inverse hyperbolic sine of a
	    * <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     asinh(z)  =  log(z + sqrt(z*z + 1))
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>asinh(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     k*<b>PI</b>*<b><i>i</i></b> + (-1)<sup><font size=-1>k</font></sup>  * <b>A</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * @return                  Principal <tt>Complex</tt> inverse hyperbolic sine
	    * <p>
	    * @see                     Complex#sinh()
	    **/
	    public Complex asinh () {
	        Complex result;
	            //  asinh(z)  =  log(z + sqrt(z*z + 1))

	            double _re1, _im1;

	            // _1:      z.mul(z).add(one) ...
	            _re1 =  ( (real*real) - (imaginary*imaginary) ) + 1.0;
	            _im1 =  ( (real*imaginary) + (imaginary*real) ) + 0.0;

	            // result:  _1.sqrt() ...
	            result =  new Complex(_re1, _im1);
	            result = result.sqrt();

	            // result:  z.add(result) ...
	            result.real =  real + result.real;                                       // !
	            result.imaginary =  imaginary + result.imaginary;                                       // !

	            // _1:      result.log() ...
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  _1 ...
	            result.real =  _re1;
	            result.imaginary =  _im1;
	        return  result;
	    }//end asinh()



	    /**
	    * Returns the <i>principal</i> inverse hyperbolic cosine of a
	    * <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     acosh(z)  =  log(z + sqrt(z*z - 1))
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>acosh(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     2*k*<b>PI</b>*<b><i>i</i></b> +/- <b>A</b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * @return                  Principal <tt>Complex</tt> inverse hyperbolic cosine
	    * <p>
	    * @see                     Complex#cosh()
	    **/
	    public Complex acosh () {
	        Complex result;
	            //  acosh(z)  =  log(z + sqrt(z*z - 1))

	            double _re1, _im1;

	            // _1:  z.mul(z).sub(one) ...
	            _re1 =  ( (real*real) - (imaginary*imaginary) ) - 1.0;
	            _im1 =  ( (real*imaginary) + (imaginary*real) ) - 0.0;

	            // result:  _1.sqrt() ...
	            result =  new Complex(_re1, _im1);
	            result = result.sqrt();

	            // result:  z.add(result) ...
	            result.real =  real + result.real;                                       // !
	            result.imaginary =  imaginary + result.imaginary;                                       // !

	            // _1:  result.log() ...
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  _1 ...
	            result.real =  _re1;
	            result.imaginary =  _im1;
	        return  result;
	    }//end acosh()



	    /**
	    * Returns the <i>principal</i> inverse hyperbolic tangent of a
	    * <tt>Complex</tt> number.
	    *
	    * <p>
	    * <pre>
	    *     atanh(z)  =  1/2 * log( (1+z)/(1-z) )
	    * </pre>
	    * <p>
	    * There are infinitely many solutions, besides the principal solution.
	    * If <b>A</b> is the principal solution of <i>atanh(z)</i>, the others are
	    * of the form:
	    * <p>
	    * <pre>
	    *     <b>A</b> + k*<b>PI</b>*<b><i>i</i></b>
	    * </pre>
	    * <p>
	    * where k is any integer.
	    * <p>
	    * <i><b>Domain Restrictions:</b><ul> atanh(z) is undefined for z = + 1 or z = - 1
	    * </ul></i>
	    * <p>
	    * @return                  Principal <tt>Complex</tt> inverse hyperbolic tangent
	    * <p>
	    * @see                     Complex#tanh()
	    **/
	    public Complex atanh () {
	        Complex result;
	            //  atanh(z)  =  1/2 * log( (1+z)/(1-z) )

	            double _re1, _im1;

	            // result:  one.add(z) ...
	            result =  new Complex(1.0 + real, + imaginary);

	            // _1:      one.sub(z) ...
	            _re1 =  1.0 - real;
	            _im1 =  - imaginary;

	            // result:  result.div(_1) ...
	            result = result.div(new Complex(_re1, _im1));

	            // _1:      result.log() ...
	            _re1 =  Math.log(result.abs());
	            _im1 =  result.arg();

	            // result:  _1.scale(0.5) ...
	            result.real =  0.5 * _re1;
	            result.imaginary =  0.5 * _im1;
	        return  result;
	    }//end atanh()



	    /**
	    * Converts a <tt>Complex</tt> into a <tt>String</tt> of the form
	    * <tt>(</tt><i>a</i><tt> + </tt><i>b</i><tt>i)</tt>.
	    *
	    * <p>
	    * This enables the <tt>Complex</tt> to be easily printed.  For example, if
	    * <tt>z</tt> was <i>2 - 5<b>i</b></i>, then
	    * <pre>
	    *     System.out.println("z = " + z);
	    * </pre>
	    * would print
	    * <pre>
	    *     z = (2 - 5i)
	    * </pre>
	    * <!--
	    * <i><b>Note:</b><ul>Concatenating <tt>String</tt>s, using a system
	    * overloaded meaning of the "<tt>+</tt>" operator, in fact causes the
	    * <tt>toString()</tt> method to be invoked on the object <tt>z</tt> at
	    * runtime.</ul></i>
	    * -->
	    * <p>
	    * @return                  <tt>String</tt> containing the cartesian coordinate representation
	    * <p>
	    **/
	    public String toString () {
	        if (imaginary < 0.0) {                                                        // ...remembering NaN & Infinity
	            return  ("(" + real + " - " + (-imaginary) + "i)");
	        } else if (1.0/imaginary == Double.NEGATIVE_INFINITY) {
	            return  ("(" + real + " - " + 0.0 + "i)");
	        } else {
	            return  ("(" + real + " + " + (+imaginary) + "i)");
	        }//endif
	    }//end toString()
	    

	     static private void
	     inv (Complex z) {
	         double zRe, zIm;
	         double scalar;

	         if (Math.abs(z.real) >= Math.abs(z.imaginary)) {
	             scalar =  1.0 / ( z.real + z.imaginary*(z.imaginary/z.real) );

	             zRe =    scalar;
	             zIm =    scalar * (- z.imaginary/z.real);
	         } else {
	             scalar =  1.0 / ( z.real*(z.real/z.imaginary) + z.imaginary );

	             zRe =    scalar * (  z.real/z.imaginary);
	             zIm =  - scalar;
	         }//endif

	         z.real = zRe;
	         z.imaginary = zIm;
	     }//end inv(Complex)
}
