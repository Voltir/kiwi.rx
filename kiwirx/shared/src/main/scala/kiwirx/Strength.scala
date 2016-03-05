package kiwirx


case class Strength(value: Double) extends AnyVal

object Strength {

  val REQUIRED = create(1000.0,1000.0,1000.0)

  val STRONG = create(1.0,0.0,0.0)

  val MEDIUM = create(0.0,1.0,0.0)

  val WEAK = create(0.0,0.0,1.0)

  def create(a: Double, b: Double, c: Double, w: Double): Double = {
    (Math.max(0.0,Math.min(1000.0,a*w)) * 1000000.0) +
    (Math.max(0.0,Math.min(1000.0,b*w)) * 1000.0) +
    (Math.max(0.0,Math.min(1000.0,c*w)) * 1.0)
  }

  def create(a: Double, b: Double, c: Double): Strength = {
    Strength(create(a,b,c,1.0))
  }

  def clip(str: Strength) = Strength {
    Math.max(0.0, Math.min(REQUIRED.value,str.value))
  }
}
