package kiwirx

class Constraint(val expr: Expression, val str: Strength)

object Constraint {
  def apply(expr: Expression) = {
    println(s"NEW CONSTRAINT: $expr)")
    new Constraint(expr,Strength.REQUIRED)
  }
}