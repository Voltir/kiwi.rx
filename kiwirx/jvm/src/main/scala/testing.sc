import kiwirx2.Coefficient
import kiwirx2.implicits._
import rx._
import pprint.pprintln
sealed trait Variable

class Slack()(implicit name: sourcecode.Name) extends Variable {
  override def toString: String = name.value
}

import scala.annotation.elidable
import scala.annotation.elidable.ASSERTION
import scala.collection.mutable
@elidable(ASSERTION)
def please(chk: Boolean, msg: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
  if(!chk) throw new IllegalArgumentException(s"${file.value}(${line.value}): $msg")
}



//Represents: a*x1 + b*x2 ... c
class Row {
  var constant = Coefficient(0.0)
  private val extraCells = mutable.AnyRefMap.empty[Variable,Coefficient]
  private val varCells = mutable.AnyRefMap.empty[Var[Double],Coefficient]

  def solveFor(variable: Variable): Unit = {
    please(contains(variable), s"Invalid Variable! $variable")
    val coeff = -1.0 / extraCells(variable)
    constant *= coeff
    extraCells.remove(variable)
    extraCells.foreach(c => extraCells.update(c._1,c._2*coeff))
    varCells.foreach(v => varCells.update(v._1,v._2*coeff))
  }

  def solveFor(variable: Var[Double]): Unit = {
    please(contains(variable), s"Invalid Variable! $variable")
    val coeff = -1.0 / varCells(variable)
    constant *= coeff
    varCells.remove(variable)
    extraCells.foreach(c => extraCells.update(c._1,c._2*coeff))
    varCells.foreach(v => varCells.update(v._1,v._2*coeff))
  }

  //in kiwi-java 'variable' can already be in the row - i might want that behavior?
  def insert(variable: Variable, coefficient: Coefficient): Unit = {
    please(!contains(variable), s"Repeat insert of $variable")
    please(!coefficient.nearZero,s"Can't insert a variable near zero! $variable")
    extraCells.put(variable,coefficient)
  }

  def insert(variable: Var[Double], coefficient: Coefficient): Unit = {
    please(!contains(variable), s"Repeat insert of $variable")
    please(!coefficient.nearZero,s"Can't insert a variable near zero! $variable")
    varCells.put(variable,coefficient)
  }

  def contains(variable: Variable): Boolean = extraCells.contains(variable)

  def contains(variable: Var[Double]): Boolean = varCells.contains(variable)

  //This is a no-op if basic.v is not contained in kiwi-java, im attempting to make it throw an error
  def substitute(basic: Basic): Unit = {
    please(contains(basic.v), "Target basic variable not in this row!")
    varCells(basic.v)
  }

  def pivot: Option[Var[Double]] = {
//    var best = Option.empty[Var[Double]]
//    var bestWeight = Double.MaxValue
//    varCells.filter(_._2.value < 0.0).foreach { case (a,b) =>
//      println("??")
//      val wurt = constant.value / b.value
//      println(wurt)
//      if(wurt > 0.0 && wurt < bestWeight) {
//        best = Option(a)
//        bestWeight = wurt
//      }
//    }
//    best
    val negVars = varCells.filter(_._2.value < 0.0)
    if(negVars.isEmpty) None
    else Option(negVars.minBy(_._2.value)._1)
  }

  override def toString: String = {
    def elem(k: String, c: Coefficient): String = if(c.value > 0) s" + ${c.value}$k" else s" - ${-c.value}$k"
    val names =
      (varCells.map(a => (a._1.toString,a._2)) ++
      extraCells.map(a => (a._1.toString,a._2))).toList
    names.map(a => elem(a._1,a._2)).mkString("")
  }
}

object Row {
  def apply(coefficient: Coefficient)(args: (Var[Double],Coefficient) *): Row = {
    val row = new Row()
    args.foreach(a => row.insert(a._1,a._2))
    row
  }

  def apply(args: (Var[Double],Coefficient) *): Row = {
    val row = new Row()
    args.foreach(a => row.insert(a._1,a._2))
    row
  }

  def pprint(rows: Row*): Unit = {
    pprintln(rows)
  }
}

//Represents: v = a*x1 + b*x2 ... + c
class Basic private (val v: Var[Double], val definition: Row)
object Basic {
  def apply(variable: Var[Double], definition: Row): Basic = {
    please(!definition.contains(variable),"Definition cannot contain defined variable!")
    Basic(variable,definition)
  }
}

val a = Var(0.0)
val b = Var(0.0)
val c = Var(0.0)
val p = Var(0.0)
val s1 = new Slack
val s2 = new Slack
val objective = Row(a -> -7.0.coeff, b -> -8.0.coeff, c -> -10.0.coeff, p -> 1.0.coeff)
val r2 = Row(1000.0.coeff)(a -> 2.0.coeff, b -> 3.0.coeff, c -> 2.0.coeff)
r2.insert(s1,1.0.coeff)
val r3 = Row(800.0.coeff)(a -> 1.0.coeff, b -> 1.0.coeff, c -> 2.0.coeff)
r3.insert(s2,1.0.coeff)

Row.pprint(objective,r2,r3)

println(objective.pivot)