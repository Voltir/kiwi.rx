package kiwirx

import rx._

class Term(val variable: Var[Double], val coefficient: Double = 1.0)

object Term {
  def apply(variable: Var[Double], coefficient: Double): Term = new Term(variable,coefficient)
}
