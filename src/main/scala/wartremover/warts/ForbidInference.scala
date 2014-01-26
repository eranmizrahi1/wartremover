package org.brianmckenna.wartremover
package warts

trait ForbidInference[T] extends WartTraverser {
  def applyForbidden(u: WartUniverse)(implicit t: u.TypeTag[T]): u.Traverser = {
    import u.universe._

    val tSymbol = typeOf[T].typeSymbol
    new Traverser {
      override def traverse(tree: Tree) {
        val synthetic = isSynthetic(u)(tree)
        def error() = u.error(tree.pos, s"Inferred type containing ${tSymbol.name}")

        tree match {
          case tpt @ TypeTree() if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) =>
            error()
          case ValDef(_, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) && synthetic =>
          case DefDef(_, _, _, _, tpt: TypeTree, _) if wasInferred(u)(tpt) && tpt.tpe.contains(tSymbol) && synthetic =>
          case _ =>
            super.traverse(tree)
        }
      }
    }
  }
}