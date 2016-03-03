package kiwirx

import utest._
import rx._

object BasicTests extends TestSuite {
  implicit val testctx = Ctx.Owner.safe()

  def tests = TestSuite {
    'simple1 {
      assert(1 == 1)
    }
  }
}
