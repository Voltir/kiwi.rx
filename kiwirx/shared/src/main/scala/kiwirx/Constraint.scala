package kiwirx

class Constraint(val expr: Expression, val str: Strength)

object Constraint {
  def apply(expr: Expression) = new Constraint(expr,Strength.REQUIRED)
}