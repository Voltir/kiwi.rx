package rx.cassowary.core

sealed trait SimplexVariable

class Slack()(implicit name: sourcecode.Name) extends SimplexVariable {
  override def toString: String = name.value
}