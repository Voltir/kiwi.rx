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
    'simple1 {
      val solver = new Solver()
      val x = Var(0.0)
      solver.addConstraint(Symbolics.equals(Symbolics.add(x,2),20))

      solver.updateVariables()

      println(x.now)
      println(x.now)
      println(x.now)
      println(x.now)
      //solver.addConstraint(Equals(Add(x,2),20.0))
    }
  }
}
