package kiwirx

import scala.collection.{mutable => m}
class Expression(val constant: Double, val terms: m.Buffer[Term]) {
}
