package kiwirx

import rx._

class Term(val variable: External, val coefficient: Double) {
  override def toString(): String = s"${coefficient}x$variable"
}

object Term {
  def apply(variable: Var[Double], coefficient: Double = 1.0): Term = new Term(External(variable),coefficient)
}
