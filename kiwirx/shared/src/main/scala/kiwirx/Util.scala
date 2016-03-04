package kiwirx

object Util {
  val EPS: Double = 1.0e-8
  def nearZero(value: Double) = if(value < 0.0) -value < EPS else value < EPS
}
