package kiwirx2.internal

import rx._

class VarLatch[T](latchFunc: Var[T] => Unit) { //(implicit ctx: Ctx.Owner) {

  private var ready: Boolean = false

  //private val _owner = Rx { () => () }

  private def _latch(t: Var[T]) = {
    if(ready) {
      ready = false
      latchFunc(t)
    }
  }

  def include(included: rx.Var[T])(implicit ctx: Ctx.Owner): Unit = {
    included.Internal.observers.add(new Obs({() =>
      _latch(included)
    }, included))
  }

  def reset(): Unit = ready = true
}

