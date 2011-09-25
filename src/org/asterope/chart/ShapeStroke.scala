/*
Copyright 2006 Jerry Huxtable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package org.asterope.chart

import scala.List
import java.awt._
import java.awt.geom._

/**Java2D stroke which paints shapes along its path. See
 * http://www.jhlabs.com/java/java2d/strokes/
 */
class ShapeStroke(shapes2:List[Shape], advance:Float) extends Stroke {

  def this(shape:Shape, advance:Float) = this(List(shape),advance)

  private var t = new AffineTransform

  private val shapes:Array[Shape] = shapes2.map{s=>
    val bounds = s.getBounds2D
    t.setToTranslation(-bounds.getCenterX, -bounds.getCenterY)
    t.createTransformedShape(s)

  }.toArray


  def createStrokedShape(shape: Shape): Shape = {
    val result = new GeneralPath
    val it = new FlatteningPathIterator(shape.getPathIterator(null), 1)
    val points = new Array[Float](6)
    var lastX: Float = 0
    var lastY: Float = 0
    var next: Float = 0
    var currentShape: Int = 0
    var length: Int = shapes.length
    while (currentShape < length && !it.isDone) {
      it.currentSegment(points) match {
        case PathIterator.SEG_MOVETO => {
          lastX = points(0)
          lastY = points(1)
          result.moveTo(points(0), points(1))
          next = 0
        }
        case PathIterator.SEG_CLOSE => {}

        case PathIterator.SEG_LINETO => {
          val dx: Float = points(0) - lastX
          val dy: Float = points(1) - lastY
          val distance: Float = math.sqrt(dx * dx + dy * dy).toFloat
          if (distance >= next) {
            val angle: Float = math.atan2(dy, dx).toFloat
            while (currentShape < length && distance >= next) {
              val x: Float = lastX + next * dx / distance
              val y: Float = lastY + next * dy / distance
              t.setToTranslation(x, y)
              t.rotate(angle)
              result.append(t.createTransformedShape(shapes(currentShape)), false)
              next += advance
              currentShape += 1
              currentShape %= length
            }
          }
          next -= distance
          lastX = points(0)
          lastY = points(1)
        }
      }
      it.next
    }
    return result
  }


}

