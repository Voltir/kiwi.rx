package kiwirx

class Term(val coefficient: Double = 1.0)

object Term {
  def apply(coefficient: Double): Term = new Term(coefficient)
}
