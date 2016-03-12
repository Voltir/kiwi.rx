package kiwirx

import collection.{mutable => m}
import rx._

class Solver {

  class Tag(var marker: Option[Variable] = None, var other: Option[Variable] = None)

  private val cns: m.Set[Constraint] = m.Set.empty
  private val rows: m.Map[Variable,Row] = m.LinkedHashMap.empty

  //private val infeasibleRows = m.Buffer.empty[Symbol]

  private val objective: Row = Row()
  //private var artificial = Option.empty[Row]

  def addConstraint(constraint: Constraint): Unit = {
    println("==== ADD CONSTRAINT START ====")
    require(!cns.contains(constraint),"Duplicate Constraint!")

    val tag = new Tag()
    val row = createRow(constraint, tag)
    val subject = chooseSubject(row, tag)

    println("Created row: " + row)
    println("Chosen subject: " + subject)

    row.solveFor(subject)
    substitute(subject,row)
    rows.put(subject,row)

    ////
    cns.add(constraint)

    println("--BEFORE OPTIMIZE--")
    println(rows)
    optimize(objective)
    println("--AFTER OPTIMIZE--")
    println(rows)
    println("")

    //Update vars after new constraint is added
    Var.set(rows.iterator.flatMap {
      case ((ext:External),c) =>
        //ext.v -> (c.constant + c.cells.foldLeft(0.0)((acc,r) => acc + r._1.v * r._2))
        Option(ext.v -> c.constant)
      case _ =>
        None
    }.toSeq:_*)
  }


  private def createRow(constraint: Constraint, tag: Tag): Row = {
    val row = Row(constraint.expr.constant)
    constraint.expr.terms.view.filter(t => !Util.nearZero(t.coefficient)).foreach { t =>
      rows.get(t.variable)
          .fold(row.insert(t.variable,t.coefficient))(other => { row.insert(other,t.coefficient)})
    }
    println("AA? " + row)
    constraint.op match {
      case Constraint.LE | Constraint.GE =>
        val coeff = if(constraint.op == Constraint.LE) 1.0 else -1.0
        println("coeff = " + coeff)
        val slack = Slack(Var(0.0))
        tag.marker = Option(slack)
        row.insert(slack,coeff)
        if(constraint.str < Strength.REQUIRED) {
          val error = Error(Var(0.0))
          tag.other = Option(error)
          row.insert(error,-coeff)
          println("BB? " + row)
          objective.insertWat(error,constraint.str)
        }

      case Constraint.EQ =>
        if(constraint.str < Strength.REQUIRED) {
          val errplus = Error(Var(0.0))
          val errminus = Error(Var(0.0))
          tag.marker = Option(errplus)
          tag.other = Option(errminus)
          row.insert(errplus,-1.0)
          row.insert(errminus,1.0)
          objective.insertWat(errplus,constraint.str)
          objective.insertWat(errminus,constraint.str)
        } else {
          val dummy = Dummy()
          tag.marker = Option(dummy)
          row.insert(dummy)
        }
    }

    // Ensure the row as a positive constant.
    if(row.constant < 0.0) {
      row.reverseSign()
    }

    row
  }

  private def optimize(localObjective: Row): Unit = {
    println("OPTIMIZE: " + objective)
    while(true) {
      println("...optimize...")
      val maybeEntering = getEnteringVariable(localObjective)
      println(maybeEntering)
      if(maybeEntering.isEmpty) return

      val entering = maybeEntering.get
      val entry = getLeavingRow(entering)

      var leaving: Variable = null
      rows.filter(_._2 == entry).foreach { wat =>
        leaving = wat._1
      }
//
//      var entryKey: Symbol = null
//      rows.keysIterator.filter(_ == entry).foreach { key =>
//        println("Optimize (entryKey)?  " + key)
//        entryKey = key
//      }

      //require(entryKey != null,"entryKey was null?")
      //require(leaving != null,"leaving was null?")
      rows.remove(entering)
      entry.solveFor(leaving,entering)
      substitute(entering,entry)
      rows.put(entering,entry)
    }
  }

  private def getEnteringVariable(localObjective: Row): Option[Variable] = {
    localObjective.cells.find { case (k,v) =>
      (!k.isInstanceOf[Dummy]) && v < 0.0
    }.map(_._1)
  }


  //private def getEnteringSymbol(objective: Row): Symbol = {
  //  objective.cells.find { case (k,v) => k.t == Symbol.DUMMY && v < 0.0 }.fold(Symbol())(_._1)
  //}

  //TODO - Option[Row] ???
  private def getLeavingRow(entering: Variable): Row = {
    var ratio: Double = Double.MaxValue
    var row: Row = null
    //rows.keysIterator.filter(_.t != Symbol.EXTERNAL).foreach { key =>
    rows.keysIterator.filter(!_.isInstanceOf[External]).foreach { key =>
      val candidate = rows(key)
      val temp = candidate.coefficientFor(entering)
      if(temp < 0.0) {
        val temp_ratio = -candidate.constant / temp
        if(temp_ratio < ratio) {
          ratio = temp_ratio
          row = candidate
        }
      }
    }
    require(row != null,"getLeavingRow -- NO ROW FOUND!?")
    row
  }

  /**
    * Choose the subject for solving for the row
    * <p/>
    * This method will choose the best subject for using as the solve
    * target for the row. An invalid symbol will be returned if there
    * is no valid target.
    * The symbols are chosen according to the following precedence:
    * 1) The first symbol representing an external variable.
    * 2) A negative slack or error tag variable.
    * If a subject cannot be found, an invalid symbol will be returned.
    */
  private def chooseSubject(row: Row, tag: Tag): Variable = {
    /*    require(tag.other != null,"chooseSubject: tag.other was null?")
    row.cells.keysIterator.find(_.t == Symbol.EXTERNAL).getOrElse {
      if(tag.marker.t == Symbol.SLACK || tag.marker.t == Symbol.ERROR && row.coefficientFor(tag.marker) < 0.0) {
        tag.marker
      } else if(tag.other.t == Symbol.SLACK || tag.other.t == Symbol.ERROR && row.coefficientFor(tag.other) < 0.0) {
        tag.other
      } else {
        Symbol()
      }
    }*/

    //row.cells.keys.head
    row.cells.keysIterator.collectFirst {
      case ext: External => ext
    }.getOrElse {
      if(tag.marker.exists(m => (m.isInstanceOf[Slack] || m.isInstanceOf[Error]) && row.coefficientFor(m) < 0.0)) {
        tag.marker.get
      } else if(tag.other.exists(o => (o.isInstanceOf[Slack] || o.isInstanceOf[Error]) && row.coefficientFor(o) < 0.0)) {
        tag.other.get
      } else {
        assert(false,"Could not chooser subject - should this return an option?")
        ???
      }
    }
  }

  private def substitute(v: Variable, row: Row): Unit = {
    rows.foreach { case (key,entry) =>
      entry.substitute(v,row)
      if(!key.isInstanceOf[External] && entry.constant < 0.0) {
        assert(false,"Found infesiable entry!?")
      }
      //if(key.t != Symbol.EXTERNAL && entry.constant < 0.0) {
      //  infeasibleRows.append(key)
      //}
    }
    objective.substitute(v,row)
  }

}