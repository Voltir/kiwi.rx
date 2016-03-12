package kiwirx

import rx._

sealed trait Variable
case class External(v: Var[Double]) extends Variable
case class Slack(v: Var[Double]) extends Variable
case class Error(v: Var[Double]) extends Variable
case class Dummy() extends Variable
