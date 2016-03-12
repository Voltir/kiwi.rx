package kiwirx

import rx._
import collection.{mutable => m}

class Row(var constant: Double, val cells: m.Map[Variable,Double]) {

  def add(value: Double): Double = {
    constant += value
    constant
  }

  def insert(v: Variable, coefficient: Double): Unit = {
    val next = cells.get(v).fold(coefficient)(_ + coefficient)
    if(Util.nearZero(next)) cells.remove(v)
    else cells.put(v,next)
  }

  def insert(v: Variable): Unit = insert(v,-1.0)

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

  //This is odd...
  def insertWat(v: Variable, str: Strength): Unit = {
    insert(v,str.value)
  }

  def reverseSign(): Unit = {
    constant = -constant
    cells.foreach { case (k,v) =>
      cells.put(k,-v)
    }
  }

  def solveFor(v: Variable): Unit = {
    require(cells.contains(v),"solveFor -- sym not in cells!")
    val coeff = -1.0 / cells(v)
    cells.remove(v)
    constant *= coeff
    val newCells = m.LinkedHashMap.empty[Variable,Double]
    cells.foreach { case (k,vt) => newCells.put(k,vt*coeff) }
    //cells = newCells
    cells.foreach { case (k,vt) => cells.put(k,vt*coeff) }
    assert(cells == newCells)
  }

  def solveFor(lhs: Variable, rhs: Variable): Unit = {
    insert(lhs,-1.0)
    solveFor(rhs)
  }

  def coefficientFor(v: Variable): Double = cells.getOrElse(v,0.0)

  def substitute(v: Variable, row: Row): Unit = {
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
