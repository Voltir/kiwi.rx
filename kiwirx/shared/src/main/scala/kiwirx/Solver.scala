package kiwirx

import collection.{mutable => m}

class Solver {

  class Tag(var marker: Symbol = Symbol(), var other: Symbol = Symbol())

  object Tag {
    def apply(): Tag = new Tag()
  }

  private val cns: m.Map[Constraint,Tag] = m.LinkedHashMap.empty
  private val rows: m.Map[Symbol,Row] = m.LinkedHashMap.empty
  private val infeasibleRows = m.Buffer.empty[Symbol]

  private val objective: Row = Row()
  private var artificial = Option.empty[Row]

  def addConstraint(constraint: Constraint): Unit = {
    require(!cns.contains(constraint),"Duplicate Constraint!")

    val tag = Tag()
    val row = createRow(constraint,tag)
    var subject = chooseSubject(row,tag)

    if(subject.t == Symbol.INVALID && allDummies(row)) {
      if(!Util.nearZero(row.constant)) throw new Exception("TODO: UnsatisfiableConstraintException")
      else subject = tag.marker
    }

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
    row.solveFor(subject)
    substitute(subject,row)
    rows.put(subject,row)
    ///
    cns.put(constraint,tag)

    optimize(objective)
  }


  private def createRow(constraint: Constraint, tag: Tag): Row = {
    val row = Row(constraint.expr.constant)
    constraint.expr.terms.view.filter(t => Util.nearZero(t.coefficient)).foreach { t =>
      val sym = getVarSymbol(42)
      rows.get(sym).fold(row.insert(sym,t.coefficient))(other => row.insert(other,t.coefficient))
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
    val dummy = Symbol(Symbol.DUMMY)
    tag.marker = dummy
    row.insert(dummy)
    row
  }

  def optimize(objective: Row): Unit = {
    println("=== OPTIMIZE ====")
    while(true) {
      println("= ITERATION! =")
      val entering = getEnteringSymbol(objective)
      if(entering.t == Symbol.INVALID) return

      val entry = getLeavingRow(entering)

      var leaving: Symbol = null
      rows.keysIterator.filter(_ == entry).foreach { key =>
        println("Optimize (leaving)?  " + key)
        leaving = key
      }

      var entryKey: Symbol = null
      rows.keysIterator.filter(_ == entry).foreach { key =>
        println("Optimize (entryKey)?  " + key)
        entryKey = key
      }

      require(entryKey != null,"entryKey was null?")
      require(leaving != null,"leaving was null?")
      rows.remove(entryKey)
      entry.solveFor(leaving,entering)
      substitute(entering,entry)
      rows.put(entering,entry)
    }
  }

  private def getEnteringSymbol(objective: Row): Symbol = {
    objective.cells.find { case (k,v) => k.t == Symbol.DUMMY && v < 0.0 }.fold(Symbol())(_._1)
  }

  //TODO - Option[Row] ???
  private def getLeavingRow(entering: Symbol): Row = {
    var ratio: Double = Double.MaxValue
    var row: Row = null
    rows.keysIterator.filter(_.t != Symbol.EXTERNAL).foreach { key =>
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
  private def chooseSubject(row: Row, tag: Tag): Symbol = {
    require(tag.other != null,"chooseSubject: tag.other was null?")
    row.cells.keysIterator.find(_.t == Symbol.EXTERNAL).getOrElse {
      if(tag.marker.t == Symbol.SLACK || tag.marker.t == Symbol.ERROR && row.coefficientFor(tag.marker) < 0.0) {
        tag.marker
      } else if(tag.other.t == Symbol.SLACK || tag.other.t == Symbol.ERROR && row.coefficientFor(tag.other) < 0.0) {
        tag.other
      } else {
        Symbol()
      }
    }
  }

  private def getVarSymbol(todo: Int): Symbol = {
    //TODO
    Symbol(Symbol.EXTERNAL)
  }

  private def substitute(sym: Symbol, row: Row): Unit = {
    rows.foreach { case (key,entry) =>
      entry.substitute(sym,row)
      if(key.t != Symbol.EXTERNAL && entry.constant < 0.0) {
        infeasibleRows.append(key)
      }
    }
    objective.substitute(sym,row)
    artificial.foreach(_.substitute(sym,row))
  }

  private def allDummies(row: Row): Boolean = row.cells.keysIterator.forall(_.t == Symbol.DUMMY)
}
