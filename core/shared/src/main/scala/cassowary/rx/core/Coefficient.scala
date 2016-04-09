package cassowary.rx.core

case class Coefficient(value: Double) extends AnyVal {
  def *(other: Coefficient): Coefficient = Coefficient(value * other.value)
  def +(other: Coefficient): Coefficient = Coefficient(value + other.value)
  def unary_- = Coefficient(-value)
  def nearZero: Boolean = if(value < 0.0) -value < EPS else value < EPS
}
