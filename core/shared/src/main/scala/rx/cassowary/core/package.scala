package rx.cassowary

import scala.annotation.elidable
import scala.annotation.elidable._

package object core {
  val EPS: Double = 1.0e-8

  @elidable(ASSERTION)
  def please(chk: Boolean, msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
    if(!chk) throw new IllegalArgumentException(s"${file.value}(${line.value}): $msg")
  }

  implicit class CoefficientExt(val inp: Double) extends AnyVal {
    def coeff: Coefficient = Coefficient(inp)
    def / (other: Coefficient): Coefficient = Coefficient(inp/other.value)
  }
}
