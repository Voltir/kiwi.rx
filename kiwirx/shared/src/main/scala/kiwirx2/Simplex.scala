package kiwirx2

import acyclic.file
import rx.Var
import rx.cassowary.core.Coefficient
import rx.cassowary.core.Row

import scala.annotation.tailrec

object Simplex {

  def pivotElement(row: Row): Option[Var[Double]] = {
    val negVars = row.cells.filter(_._2.value < 0.0)
    if(negVars.isEmpty) None
    else Option(negVars.minBy(_._2.value)._1)
  }

  private def pivotWeight(chk: Var[Double], row: Row): Double = {
    row.cells.get(chk).map(c => row.constant.value / c.value).getOrElse(Double.MaxValue)
  }

  def pivotRow(pivot: Var[Double], rows: Row *): Row = {
    please(rows.nonEmpty, "Can't select pivot rows from an empty set!")
    var best = rows.head
    var bestWeight = pivotWeight(pivot,best)
    rows.foreach { row =>
      val weight = pivotWeight(pivot,row)
      if(pivotWeight(pivot,row) < bestWeight && weight > 0.0) { best = row; bestWeight = weight }
    }
    best
  }

  private def zeroop2(coefficient: Coefficient, src: Row, target: Row): Unit = {
    val c = -coefficient
    src.forcells { case (sv,sc) =>
      val newSc = sc * c
      target.cells.get(sv).map { case tc =>
        val newCoeff = newSc + tc
        if(!newCoeff.nearZero) target.insert(sv,newCoeff)
      }.getOrElse{
        target.insert(sv,newSc)
      }
    }
    target.constant = src.constant * c + target.constant
  }

  private def zeroop(coeff: Coefficient, src: Row, target: Row) = {
    println(s"Update is ${-coeff.value}xSrc + Dst -> Dst")
    src.cells.foreach { case (v,sc) =>
      val newSc = sc * -coeff
      target.cells.get(v).map { tc =>
        val newCoeff= newSc + tc
        if(!newCoeff.nearZero) target.insert(v,newCoeff)
        else target.remove(v)
      }.getOrElse {
        target.insert(v,newSc)
      }
    }
    src.simplexCells.foreach { case (v,sc) =>
      val newSc = sc * -coeff
      target.simplexCells.get(v).map { tc =>
        val newCoeff = newSc + tc
        if(!newCoeff.nearZero) target.insert(v,newCoeff)
        else target.remove(v)
      }.getOrElse {
        target.insert(v,newSc)
      }
    }
    target.constant = (src.constant * -coeff) + target.constant
  }

  def iteration(objective: Row, pivot: Var[Double])(rows: Row *): Unit = {
    println(s"#### ITERATION ($pivot) ####")
    val row = pivotRow(pivot,rows:_*)
    println("-- pivot row: " + row)
    //Make pivot = 1.0 in pivot row
    //row *= row(pivot)
    row.get(pivot).foreach { pc => row *= 1.0 / pc }
    println(s"Make one for ($pivot): " + row)

    //Zero out pivot in remaining rows
    rows.filter(_ != row).foreach { other =>
      other.get(pivot).foreach { oc => zeroop(oc,row,other)}
      println(other.contains(pivot))
    }
    objective.get(pivot).foreach(oc => zeroop(oc,row,objective))
    println("---- Fin Iteration ----")
    pprint.pprintln(objective :: rows.toList)
  }

  @tailrec
  def solve(objective: Row)(rows: Row *): Unit = {
    val pivot = pivotElement(objective)
    if(pivot.isDefined) {
      iteration(objective,pivot.get)(rows:_*)
      solve(objective)(rows:_*)
    }
  }
}
