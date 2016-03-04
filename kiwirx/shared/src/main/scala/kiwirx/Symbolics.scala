package kiwirx
import collection.{mutable => m}

object Symbolics {

  def add(first: Expression, second: Expression): Expression = {
    val terms = first.terms ++ second.terms
    new Expression(first.constant + second.constant, terms)
  }

  def multiply(term: Term, coefficient: Double): Term = {
    new Term(term.coefficient*coefficient)
  }

  def multiply(expression: Expression, coefficient: Double): Expression = {
    val terms = expression.terms.map(t => multiply(t,coefficient))
    new Expression(expression.constant * coefficient, terms)
  }

  def negate(expression: Expression): Expression = multiply(expression,-1.0)

  def subtract(first: Expression, second: Expression): Expression = add(first,negate(second))

  trait Expr[A] {
    def reduce(a: A): Expression
  }

  class Equals(val first: Expression, val second: Expression)

  object Equals {
    def apply[A: Expr, B: Expr](a: A, b: B): Equals =
      new Equals(
        implicitly[Expr[A]].reduce(a),
        implicitly[Expr[B]].reduce(b)
      )

    implicit def constraint(equals: Equals): Constraint = {
      new Constraint(subtract(equals.first,equals.second),1.0)
    }
  }

  class Add[A: Expr, B: Expr](first: A, second: B)

  object Add {
    def apply[A: Expr, B: Expr](a: A, b: B) = new Add(a,b)

    implicit def ExprAdd[A: Expr, B: Expr]: Expr[Add[A,B]] = new Expr[Add[A,B]] {
      def reduce(a: Add[A,B]) = new Expression(1.0,m.Buffer.empty)
    }
  }
}
