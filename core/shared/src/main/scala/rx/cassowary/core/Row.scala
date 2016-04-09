package rx.cassowary.core

import rx._

import scala.collection.mutable

//Represents: a*x1 + b*x2 ... c
class Row()(implicit sc: sourcecode.Name) {
  import Row._

  val name = sc.value

  var constant = Coefficient(0.0)

  implicit val cells: CellMap[Var[Double]] = mutable.AnyRefMap.empty

  implicit val simplexCells: CellMap[SimplexVariable] = mutable.AnyRefMap.empty

  private def solveForImpl[V <: AnyRef, AltV <: AnyRef](variable: V)
                                                       (implicit primary: CellMap[V], alternate: CellMap[AltV]) = {
    please(primary.contains(variable), s"Invalid Variable! $variable")
    val coeff = -1.0 / primary(variable)
    constant *= coeff
    primary.remove(variable)
    primary.foreach(c => primary.update(c._1,c._2*coeff))
    alternate.foreach(v => alternate.update(v._1,v._2*coeff))
    this
  }

  def solveFor(variable: Var[Double]): Row = solveForImpl[Var[Double],SimplexVariable](variable)

  def solveFor(variable: SimplexVariable): Row = solveForImpl[SimplexVariable,Var[Double]](variable)

  def forcells(f: (Var[Double],Coefficient) => Unit): Unit = ???

  private def updateCoefficients(f: Coefficient => Coefficient) = {
    constant = f(constant)
    cells.foreach(c => cells.put(c._1,f(c._2)))
    simplexCells.foreach(c => simplexCells.put(c._1,f(c._2)))
  }

  def *=(coefficient: Coefficient): Unit = updateCoefficients(_ * coefficient)


  //in kiwi-java 'variable' can already be in the row - i might want that behavior?
  def insert(variable: SimplexVariable, coefficient: Coefficient): Row = {
    //please(!contains(variable), s"Repeat insert of $variable")
    please(!coefficient.nearZero,s"Can't insert a variable near zero! $variable")
    simplexCells.put(variable,coefficient)
    this
  }

  def insert(variable: Var[Double], coefficient: Coefficient): Row = {
    //please(!contains(variable), s"Repeat insert of $variable")
    please(!coefficient.nearZero,s"Can't insert a variable near zero! $variable")
    cells.put(variable,coefficient)
    this
  }

  def contains(variable: SimplexVariable): Boolean = simplexCells.contains(variable)

  def contains(variable: Var[Double]): Boolean = cells.contains(variable)

  def get(variable: SimplexVariable): Option[Coefficient] = simplexCells.get(variable)

  def get(variable: Var[Double]): Option[Coefficient] = cells.get(variable)

  def remove(variable: SimplexVariable): Option[Coefficient] = simplexCells.remove(variable)

  def remove(variable: Var[Double]): Option[Coefficient] = cells.remove(variable)

  //This is a no-op if basic.v is not contained in kiwi-java, im attempting to make it throw an error
  def substitute(basic: Basic): Unit = {
    please(contains(basic.v), "Target basic variable not in this row!")
    cells(basic.v)
  }

  override def toString: String = {
    def elem(k: String, c: Coefficient): String = if(c.value > 0) s"+(${c.value})$k" else s"-(${-c.value})$k"
    val names =
      (cells.map(a => (a._1.toString,a._2)) ++
        simplexCells.map(a => (a._1.toString,a._2))).toList
    s"$name@Row[]: ${names.map(a => elem(a._1,a._2)).mkString(" ")} == ${constant.value}"
  }
}

object Row {
  private type CellMap[A <: AnyRef] = mutable.AnyRefMap[A,Coefficient]

  def apply(coefficient: Coefficient)(args: (Var[Double],Coefficient) *)(implicit sc: sourcecode.Name): Row = {
    val row = new Row()
    row.constant = coefficient
    args.foreach(a => row.insert(a._1,a._2))
    row
  }

  def apply(args: (Var[Double],Coefficient) *)(implicit sc: sourcecode.Name): Row = {
    val row = new Row()
    args.foreach(a => row.insert(a._1,a._2))
    row
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
