package kiwirx



class Constraint(val expr: Expression, val str: Strength, val op: Constraint.Op) {
  def strength(newStr: Strength): Constraint = new Constraint(expr,newStr,op)
}


object Constraint {
  sealed trait Op
  case object EQ extends Op
  case object GE extends Op
  case object LE extends Op

  def apply(expr: Expression, op: Op) = {
    new Constraint(expr,Strength.REQUIRED,op)
  }
}