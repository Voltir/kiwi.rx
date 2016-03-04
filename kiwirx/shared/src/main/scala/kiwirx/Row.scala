package kiwirx

import collection.{mutable => m}

class Row(var constant: Double, var cells: m.Map[Symbol,Double]) {

  def add(value: Double): Double = {
    constant += value
    constant
  }

  def insert(sym: Symbol, coefficient: Double): Unit = {
    val next = cells.get(sym).fold(coefficient)(_ + coefficient)
    if(Util.nearZero(next)) cells.remove(sym)
    else cells.put(sym,next)
  }

  def insert(sym: Symbol): Unit = insert(sym,-1.0)

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

  def solveFor(sym: Symbol): Unit = {
    require(cells.contains(sym),"solveFor -- sym not in cells!")
    val coeff = -1.0 / cells(sym)
    cells.remove(sym)
    constant *= coeff
    val newCells = m.LinkedHashMap.empty[Symbol,Double]
    cells.foreach { case (k,v) => newCells.put(k,v*coeff) }
    cells = newCells
  }

  def solveFor(lhs: Symbol, rhs: Symbol): Unit = {
    insert(lhs,-1.0)
    solveFor(rhs)
  }

  def coefficientFor(sym: Symbol): Double = cells.getOrElse(sym,0.0)

  def substitute(sym: Symbol, row: Row): Unit = {
    cells.get(sym).foreach { coeff =>
      cells.remove(sym)
      insert(row,coeff)
    }
  }
}

object Row {
  def apply() = new Row(0, m.LinkedHashMap.empty)
  def apply(constant: Double) = new Row(constant, m.LinkedHashMap.empty)
  def apply(other: Row) = new Row(other.constant, other.cells.clone())
}
