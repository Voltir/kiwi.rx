package kiwirx

import rx._

class Term(val variable: Var[Double], val coefficient: Double) {
  override def toString(): String = s"${coefficient}x$variable"
}

object Term {
  def apply(variable: Var[Double], coefficient: Double = 1.0): Term = new Term(variable,coefficient)
}
