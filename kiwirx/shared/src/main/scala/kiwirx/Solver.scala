package kiwirx

import collection.{mutable => m}
import rx._

class Solver {

  private val cns: m.Set[Constraint] = m.Set.empty
  private val rows: m.Map[Var[Double],Row] = m.LinkedHashMap.empty

  private val infeasibleRows = m.Buffer.empty[Symbol]

  private val objective: Row = Row()
  private var artificial = Option.empty[Row]

  def addConstraint(constraint: Constraint): Unit = {
    println("=== add constraint ===")
    require(!cns.contains(constraint),"Duplicate Constraint!")

    val row = createRow(constraint)

    var subject = chooseSubject(row)
    println("Row: " + row)
    println("Subject: " + subject)
//    if(subject.t == Symbol.INVALID && allDummies(row)) {
//      if(!Util.nearZero(row.constant)) throw new Exception("TODO: UnsatisfiableConstraintException")
//      else subject = tag.marker
//    }

//    if (subject.getType() == Symbol.Type.INVALID) {
//      if (!addWithArtificialVariable(row)) {
//        throw new UnsatisfiableConstraintException(constraint);
//      }
//    } else {
//      row.solveFor(subject);
//      substitute(subject, row);
//      this.rows.put(subject, row);
//    }

    //if
    //else
    println("???")
    row.solveFor(subject)
    println("Row (After Solve): ")
    println(row)

    substitute(subject,row)
    println("Row (After Substitute): ")
    println(row)

    rows.put(subject,row)
    ///
    cns.add(constraint)

    optimize(objective)
    println("\n-- " + rows.mkString("\n-- "))
    rows.foreach { case (v,c) =>
      //require(c.cells.isEmpty,"Cells should all be empty!")
      v() = c.constant + c.cells.foldLeft(0.0)((acc,r) => acc + r._1.now * r._2)
    }

    println("DONE!\n\n")
  }


  private def createRow(constraint: Constraint): Row = {
    val row = Row(constraint.expr.constant)

    println("=== CREATE ROW START ===")
    println(row)
    constraint.expr.terms.view.filter(t => !Util.nearZero(t.coefficient)).foreach { t =>
      println("Checking..." + t)
      rows.get(t.variable).fold(row.insert(t.variable,t.coefficient))(other => row.insert(other,t.coefficient))
    }
    //      case OP_EQ: {
    //        if (constraint.getStrength() < Strength.REQUIRED) {
    //        Symbol errplus = new Symbol(Symbol.Type.ERROR);
    //        Symbol errminus = new Symbol(Symbol.Type.ERROR);
    //        tag.marker = errplus;
    //        tag.other = errminus;
    //        row.insert(errplus, -1.0); // v = eplus - eminus
    //        row.insert(errminus, 1.0); // v - eplus + eminus = 0
    //        this.objective.insert(errplus, constraint.getStrength());
    //        this.objective.insert(errminus, constraint.getStrength());
    //      } else {
    //        Symbol dummy = new Symbol(Symbol.Type.DUMMY);
    //        tag.marker = dummy;
    //        row.insert(dummy);
    //      }
    //        break;
    //      }
    row
  }

  private def optimize(objective: Row): Unit = {
    println("=== OPTIMIZE ====")
    while(true) {
      println("= ITERATION! =")
      //val entering = getEnteringSymbol(objective)
      //if(entering.t == Symbol.INVALID) return


      val maybeEntering = getEnteringVar(objective)
      if(maybeEntering.isEmpty) return

      val entering = maybeEntering.get
      val entry = getLeavingRow(entering)

      var leaving: Var[Double] = null
      rows.filter(_._2 == entry).foreach { wat =>
        println("Optimize (leaving)?  " + wat)
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
      println("Set here??")
    }
  }

  private def getEnteringVar(objective: Row): Option[Var[Double]] = {
    objective.cells.find { case (k,v) => v < 0.0 }.map(_._1)
  }

  //private def getEnteringSymbol(objective: Row): Symbol = {
  //  objective.cells.find { case (k,v) => k.t == Symbol.DUMMY && v < 0.0 }.fold(Symbol())(_._1)
  //}

  //TODO - Option[Row] ???
  private def getLeavingRow(entering: Var[Double]): Row = {
    var ratio: Double = Double.MaxValue
    var row: Row = null
    //rows.keysIterator.filter(_.t != Symbol.EXTERNAL).foreach { key =>
    rows.keysIterator.foreach { key =>
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
  private def chooseSubject(row: Row): Var[Double] = {
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

    row.cells.keys.head
  }

  private def substitute(v: Var[Double], row: Row): Unit = {
    rows.foreach { case (key,entry) =>
      entry.substitute(v,row)
      //if(key.t != Symbol.EXTERNAL && entry.constant < 0.0) {
      //  infeasibleRows.append(key)
      //}
    }
    objective.substitute(v,row)
    artificial.foreach(_.substitute(v,row))
  }

}