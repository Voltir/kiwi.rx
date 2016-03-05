package object kiwirx {

  val EPS: Double = 1.0e-8

  implicit class ApproxDouble(val d: Double) extends AnyVal {
    def ~==(other: Double) = Math.abs(d-other) <= EPS
  }
}
