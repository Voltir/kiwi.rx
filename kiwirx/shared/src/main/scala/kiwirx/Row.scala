package kiwirx

import rx._
import collection.{mutable => m}

class Row(var constant: Double, val cells: m.Map[Var[Double],Double]) {

  def add(value: Double): Double = {
    constant += value
    constant
  }

  def insert(v: Var[Double], coefficient: Double): Unit = {
    val next = cells.get(v).fold(coefficient)(_ + coefficient)
    if(Util.nearZero(next)) cells.remove(v)
    else cells.put(v,next)
  }

  def insert(v: Var[Double]): Unit = insert(v,-1.0)

  def insert(other: Row, coefficient: Double): Unit = {
    constant += other.constant * coefficient

    other.cells.keysIterator.foreach { s =>
      val coeff = other.cells(s) * coefficient
      cells.get(s).fold {
        if(!Util.nearZero(coeff)) cells.put(s,coeff)
      }{ value =>
        val update = coeff + value
        if(Util.nearZero(update)) cells.remove(s)
        else cells.put(s,update)
      }
    }
  }

  def solveFor(v: Var[Double]): Unit = {
    require(cells.contains(v),"solveFor -- sym not in cells!")
    val coeff = -1.0 / cells(v)
    cells.remove(v)
    constant *= coeff
    //val newCells = m.LinkedHashMap.empty[Symbol,Double]
    //cells.foreach { case (k,v) => newCells.put(k,v*coeff) }
    //cells = newCells
    cells.foreach { case (k,v) => cells.put(k,v*coeff) }
  }

  def solveFor(lhs: Var[Double], rhs: Var[Double]): Unit = {
    insert(lhs,-1.0)
    solveFor(rhs)
  }

  def coefficientFor(v: Var[Double]): Double = cells.getOrElse(v,0.0)

  def substitute(v: Var[Double], row: Row): Unit = {
    cells.get(v).foreach { coeff =>
      cells.remove(v)
      insert(row,coeff)
    }
  }

  override def toString(): String = {
    s"Row($constant, ${cells.toMap})"
  }
}

object Row {
  def apply() = new Row(0, m.LinkedHashMap.empty)
  def apply(constant: Double) = new Row(constant, m.LinkedHashMap.empty)
  def apply(other: Row) = new Row(other.constant, other.cells.clone())
}
