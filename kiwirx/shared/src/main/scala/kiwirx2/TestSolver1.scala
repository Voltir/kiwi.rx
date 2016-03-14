package kiwirx2

import rx._
import collection.{mutable => m}

sealed trait Variable1 extends Any
case class Ext(v: Var[Double]) extends AnyVal with Variable1

case class Coefficient(value: Double) extends AnyVal {
  def *(other: Coefficient): Coefficient = Coefficient(value * other.value)
  def +(other: Coefficient): Coefficient = Coefficient(value + other.value)
  def nearZero: Boolean = if(value < 0.0) -value < EPS else value < EPS
}

object implicits {
  implicit class CoefficientExt(val inp: Double) extends AnyVal {
    def / (other: Coefficient): Coefficient = Coefficient(inp/other.value)
  }
}

//Just represent 2*x() == y()
class TestSolver1 {
  import implicits._

  class Row1(var constant: Double, val cells: m.Map[Variable1,Coefficient] = m.Map.empty) {
    def solveFor(cell: Variable1): Unit = {
      require(cells.contains(cell),"can't solve for this cell!")
      val coeff = -1.0 / cells(cell)
      cells.remove(cell)
      constant *= coeff.value
      cells.foreach { case (v,cur) => cells.put(v,cur*coeff) }
    }

    def insert(other: Row1, c: Coefficient): Unit = {
      constant += other.constant*c.value
      other.cells.foreach { case (ov,oc) =>
        val coeff = oc * c
        cells.get(ov).fold(if(!coeff.nearZero) cells.put(ov,coeff)) { current =>
          val update = coeff + current
          if(update.nearZero) cells.remove(ov)
          else cells.put(ov,update)
        }
      }
    }

    def substitute(v: Variable1, row: Row1): Unit = {
      cells.get(v).foreach { c =>
        cells.remove(v)
        insert(row,c)
      }
    }

    override def toString = s"$constant + ${cells.map { case (v,c) => s"${c.value}x$v" } .mkString(" + ")}"
  }

  class TriggerRow(val v: Variable1, val row: Row1)
  /////////////////////////////////////


  private val rows: m.Map[Variable1,Row1] = m.LinkedHashMap.empty
  private var triggerRow: Option[TriggerRow] = None

  private val obs: m.Buffer[rx.Obs] = m.Buffer.empty

  private def dbgRow: String = rows.map(entry => s"${entry._1} = ${entry._2}").mkString("\n")

  def addConstraintTest(xLike: Var[Double], yLike: Var[Double])(implicit ctx: Ctx.Owner): Unit = {
    //optimization target
    //val target = new Row1(0.0)

    //approx the initial row from solver.addConstraint(Symbolics.equals(Symbolics.multiply(x,2),y))
    val x = Ext(xLike)
    val y = Ext(yLike)
    val hacky = new Row1(0.0, m.Map(
      x -> Coefficient(2.0),
      y -> Coefficient(-1.0)))
    println(hacky)
    val subject = x
    hacky.solveFor(subject)
    //substitute(target)(subject,hacky)
    rows.put(x,hacky)
    println(hacky)
    println(dbgRow)

    val xObs = xLike.triggerLater {
      println("XOBS FIRED!")
      obs.foreach(_.kill())
      obs.clear()
      val wurt = new Row1(xLike.now)

      //hack around createRow
      wurt.insert(rows(x),Coefficient(1.0))
      println("At start: " + wurt)
      wurt.solveFor(y)
      println("after solveFor: " + wurt)
        triggerRow = Option(new TriggerRow(x,wurt))
      val objective = new Row1(0.0)
      val subject = y
      substitute(objective)(subject,wurt)
      rows.put(y,wurt)
      meh()
      println("DONE!")
      println(dbgRow)
    }

    val yObs = yLike.triggerLater {
      println("YOBS FIRED!")
      obs.foreach(_.kill())
      println("Maybe?")
      obs.clear()
      val wurt = new Row1(yLike.now)
      triggerRow = Option(new TriggerRow(y,wurt))
      val objective = new Row1(0.0)
      val subject = y
      substitute(objective)(subject,wurt)
      meh()
      println("DONE!")
      println(dbgRow)
    }

    obs.append(xObs,yObs)
  }

  private def meh(): Unit = {
    rows.foreach {
      case (Ext(v),r) => v() = r.constant
      case _ => ()
    }
  }

  private def optimize(objective: Row1): Unit = {
    println("Does nothing in this case..")
  }

  private def substitute(objective: Row1)(variable: Variable1, row: Row1): Unit = {
    rows.foreach { case (v, r) =>
      println("SUBSITURE!" + v + " ::: " + r)
      r.substitute(variable,row)
      println("AFTER----!" + v + " ::: " + r)
      if(!v.isInstanceOf[Ext] && r.constant < 0.0) {
        assert(false,"Found infeasible entry!?")
      }
    }
    objective.substitute(variable,row)
  }
}
