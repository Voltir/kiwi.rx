package kiwirx

import rx._

import collection.{mutable => m}

object Symbolics {

  def add(first: Expression, second: Expression): Expression = {
    val terms = first.terms ++ second.terms
    new Expression(first.constant + second.constant, terms)
  }

  def add(first: Term, second: Double): Expression = {
    Expression(first,second)
  }

  def add(first: Var[Double], constant: Double): Expression = {
    add(Term(first),constant)
  }

  def multiply(term: Term, coefficient: Double): Term = {
    new Term(term.variable, term.coefficient*coefficient)
  }

  def multiply(expression: Expression, coefficient: Double): Expression = {
    val terms = expression.terms.map(t => multiply(t,coefficient))
    new Expression(expression.constant * coefficient, terms)
  }

  def multiply(first: Var[Double], coefficient: Double): Expression = {
    Expression(Term(first, coefficient), 0.0)
  }

  def negate(expression: Expression): Expression = multiply(expression,-1.0)

  def subtract(first: Expression, second: Expression): Expression = add(first,negate(second))

  def equals(first: Expression, second: Expression): Constraint = {
    Constraint(subtract(first,second),Constraint.EQ)
  }

  def equals(expr: Expression, constant: Double): Constraint = {
    equals(expr,Expression(constant))
  }

  def equals(expr: Expression, v: Var[Double]): Constraint = {
    equals(expr,Expression(Term(v,1.0),0.0))
  }

  def equals(v: Var[Double], c: Double): Constraint = {
    equals(Expression(Term(v),0.0),Expression(c))
  }

  def greaterOrEqual(first: Expression, second: Expression): Constraint = {
    Constraint(subtract(first,second),Constraint.GE)
  }

  def greaterOrEqual(v: Var[Double], c: Double): Constraint = {
    greaterOrEqual(Expression(Term(v,1.0),0.0),Expression(c))
  }
}
