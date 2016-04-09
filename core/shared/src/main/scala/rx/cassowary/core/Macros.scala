package rx.cassowary.core

import scala.language.experimental.macros
import scala.reflect.macros._

object Macros {

  private def duplicateSimplexVariableTree(c: blackbox.Context)(zzz: c.Tree): c.Tree = {

    object transformer extends c.universe.Transformer {
      override def transform(tree: c.Tree): c.Tree = {
        super.transform(tree)
      }
    }

    //transformer.transform(result)
    ???
  }

  
}
