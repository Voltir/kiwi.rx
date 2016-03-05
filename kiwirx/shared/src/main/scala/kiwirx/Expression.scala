package kiwirx

import rx._
import scala.collection.{mutable => m}

class Expression(val constant: Double, val terms: m.Buffer[Term]) {

  override def toString(): String = {
    s"Expression($constant, ${terms.toVector})"
  }
}

object Expression {
  def apply(constant: Double): Expression = new Expression(constant,m.Buffer.empty)
  def apply(term: Term, constant: Double): Expression = new Expression(constant, m.Buffer(term))
}
