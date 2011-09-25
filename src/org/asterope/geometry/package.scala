package org.asterope

/**
 * Geometry transformations.  
 * This package was originaly part of Skyview (skyview.geometry), was rewriten to Scala as part of Asterope projects.
 * Transformers are highly optimized, it uses prealocated arrays and matrix transforms. 
 * 
 * This package provides coordinate system transformations. Most offten it is just rotation, Bessel transforms are more complicated.
 *
 * Projections from celestial sphere to flat plane are also provided.
 *  
 * All classes are integrated in WCS (World Coordinate System) which is usable on charts, FITS images and general spherical image processing.
 */

package object geometry{}
