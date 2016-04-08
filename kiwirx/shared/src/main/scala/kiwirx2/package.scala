import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION

package object kiwirx2 {
  val EPS: Double = 1.0e-8

  @elidable(ASSERTION)
  def please(chk: Boolean, msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
    if(!chk) throw new IllegalArgumentException(s"${file.value}(${line.value}): $msg")
  }
}

