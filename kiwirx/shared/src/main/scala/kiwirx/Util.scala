package kiwirx

object Util {
  def nearZero(value: Double) = if(value < 0.0) -value < EPS else value < EPS
}
