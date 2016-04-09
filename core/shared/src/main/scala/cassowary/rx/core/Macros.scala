package cassowary.rx.core

import scala.language.experimental.macros
import scala.reflect.macros._

object Macros {

  private def duplicateSimplexVariableTree(c: blackbox.Context)(zzz: c.Tree): c.Tree = {
    import c.universe._

    object transformer extends c.universe.Transformer {
      override def transform(tree: c.Tree): c.Tree = {
        super.transform(tree)
      }
    }

    //transformer.transform(result)
    ???
  }

  
}
