package kiwirx2.internal

import rx._

class RxLatch(latchFunc: () => Unit)(implicit ctx: Ctx.Owner) {

  private var ready: Boolean = false

  private val _owner = Rx { () => () }

  private val _obs = new Obs({() =>
    if(ready) {
      ready = false
      latchFunc()
    }
  },_owner)

  def include(included: rx.Rx[_])(implicit ctx: Ctx.Owner): Unit = {
    included.Internal.observers.add(_obs)
  }

  def reset(): Unit = ready = true
}
