package kiwirx

import Symbolics._
import kiwirx2.TestSolver1
import rx._
import utest._
import acyclic.file
import collection.{mutable => m}
object BasicTests extends TestSuite {
  implicit val testctx = Ctx.Owner.safe()

  def tests = TestSuite {

    'simpleNew {
      val solver = new Solver()
      val x = Var(0.0)
      solver.addConstraint(Symbolics.equals(Symbolics.add(x,2),20))
      assert(x.now ~== 18)
    }

    'simple0 {
      val solver = new Solver()
      val x = Var(0.0)
      solver.addConstraint(Symbolics.equals(x,10))
      println(x.now)
      assert(x.now ~== 10)
    }

    'simpleVar {
      val solver = new Solver()
      val x = Var(0.0)
      val y = Var(18.0)
      def debug() = println(s"$x $y (${x.now * 2} == ${y.now})?")
      solver.addConstraint(Symbolics.equals(Symbolics.multiply(x,2),y))
      debug()
      println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^")
      solver.addConstraint(Symbolics.equals(x,10))
      debug()
      //solver.addConstraint(Symbolics.equals(x,10)) -- throws an exception currently
      //debug()
      //x() = 5
      //debug()
      //y() = 100
      //debug()
    }

    'hacky {
      val wurt = new TestSolver1
      val x = Var(0.0)
      val y = Var(18.0)
      def debug() = println(s"$x $y (${x.now * 2} == ${y.now})?")
      wurt.addConstraintTest(x,y)
      debug()
      x() = 21
      debug()
    }

    'ge0 {
      val solver = new Solver()
      val x = Var(0.0)
      solver.addConstraint(Symbolics.greaterOrEqual(x,100.0))
      assert(x.now ~== 100.0)

      solver.addConstraint(Symbolics.greaterOrEqual(x,150.0).strength(Strength.WEAK))
      assert(x.now ~== 150)

      solver.addConstraint(Symbolics.equals(x,125.0).strength(Strength.STRONG))
      assert(x.now ~== 125)
    }
  }
}
