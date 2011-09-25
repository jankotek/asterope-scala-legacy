package org.asterope.gui

/*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 2.1 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this library; if not, write to the Free Software
* Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
*/

import javax.swing.JComponent
import javax.swing.RepaintManager
import javax.swing.SwingUtilities

/**
 * <p>
 * This class is used to detect Event Dispatch Thread rule violations<br>
 * See <a
 * href="http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html">How
 * to Use Threads</a> for more info
 * </p>
 * <p>
 * This is a modification of original idea of Scott Delap<br>
 * Initial version of ThreadCheckingRepaintManager can be found here<br>
 * <a href="http://www.clientjava.com/blog/2004/08/20/1093059428000.html">Easily
 * Find Swing Threading Mistakes</a>
 * </p>
 *
 * @author Scott Delap
 * @author Alexander Potochkin
 *
 * https://swinghelper.dev.java.net/
 */

object CheckThreadViolationRepaintManager extends RepaintManager {

  def hook(){
    if (hooked) return
    RepaintManager.setCurrentManager(CheckThreadViolationRepaintManager)
    hooked = true
  }

  private var hooked: Boolean = false

  override def addInvalidComponent(component: JComponent){
    checkThreadViolations(component)
    super.addInvalidComponent(component)
  }

  override def addDirtyRegion(component: JComponent, x: Int, y: Int, w: Int, h: Int){
    checkThreadViolations(component)
    super.addDirtyRegion(component, x, y, w, h)
  }

  private def checkThreadViolations(c: JComponent){
    if (!SwingUtilities.isEventDispatchThread && c.isShowing) {
      var repaint: Boolean = false
      var fromSwing: Boolean = false
      var imageUpdate: Boolean = false
      val stackTrace = Thread.currentThread.getStackTrace
      for (st <- stackTrace) {
        if (repaint && st.getClassName.startsWith("javax.swing.")) {
          fromSwing = true
        }
        if (repaint && ("imageUpdate" == st.getMethodName)) {
          imageUpdate = true
        }
        if ("repaint" == st.getMethodName) {
          repaint = true
          fromSwing = false
        }
      }
      if (imageUpdate) {
        // assuming it is java.awt.image.ImageObserver.imageUpdate(...)
        // image was asynchronously updated, that's ok

        return
      }
      if (repaint && !fromSwing) {
        // no problems here, since repaint() is thread safe
        return
      }
      violationFound(c, stackTrace)
    }
  }

  protected def violationFound(c: JComponent, stackTrace: Array[StackTraceElement]){
    throw new IllegalAccessError("EDT violation")
  }
}