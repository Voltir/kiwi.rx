package kiwirx



class Symbol(val t: Symbol.SymbolType) {
  override def toString(): String = s"Symbol($t)"
}

object Symbol {
  sealed trait SymbolType
  case object INVALID extends SymbolType
  case object EXTERNAL extends SymbolType
  case object SLACK extends SymbolType
  case object ERROR extends SymbolType
  case object DUMMY extends SymbolType

  def apply(t: SymbolType = INVALID): Symbol = new Symbol(t)
}