package kiwirx

import Symbolics._
import rx._
import utest._
import acyclic.file
import collection.{mutable => m}
object BasicTests extends TestSuite {
  implicit val testctx = Ctx.Owner.safe()


  //implicit def VarTerm(v: Var[Double]): Expression[Var[Double]] = ???

  //implicit def ConstTerm(d: Double): Expression[Double] = ???

  //implicit val VarWat: Expr[Var[Double]] = new Expr[Var[Double]] {
  //  def reduce(e: Var[Double]) = new Expression(1.0,m.Buffer.empty)
  //}


  def tests = TestSuite {
    /*
      @Test
    public void simpleNew() throws UnsatisfiableConstraintException, DuplicateConstraintException {
        Solver solver = new Solver();
        Variable x = new Variable("x");


        solver.addConstraint(Symbolics.equals(Symbolics.add(x, 2), 20));

        solver.updateVariables();

        assertEquals(x.getValue(), 18, EPSILON);
    }

     */
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
      solver.addConstraint(Symbolics.equals(y,10))
      debug()
      //solver.addConstraint(Symbolics.equals(x,10)) -- throws an exception currently
      //debug()
    }
  }
}
