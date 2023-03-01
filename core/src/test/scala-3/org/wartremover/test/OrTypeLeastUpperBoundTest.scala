package org.wartremover
package test

import org.wartremover.warts.OrTypeLeastUpperBound
import org.scalatest.funsuite.AnyFunSuite

class OrTypeLeastUpperBoundTest extends AnyFunSuite with ResultAssertions {

  import OrTypeLeastUpperBoundTest.*

  test("All") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.All) {
      List(IArray(1), false)
      def x1 = List(A1(1), B(2))
      List("a", true)
      val x2 = List(Right[Int, Int](1), Option("a"))
    }
    assert(result.errors.size == 4)
    assert(result.errors.forall(_.contains("least upper bound is")), result)

    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.All) {
      for {
        x1 <- Option(5)
        x2 <- x1 match {
          case 2 =>
            Option(L(3))
          case _ =>
            Option(R("a"))
        }
      } yield ()
      Tuple2(
        _2 = 3,
        _1 = if (true) None else Some(2)
      )
      List(Right(1), Left(2))
      List(Some(77), None)
    }
    assertEmpty(mustEmpty)
  }

  test("Any") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Any) {
      List(IArray(1), false)
    }
    assertError(result)("least upper bound is `scala.Any`. `scala.IArray$package.IArray[scala.Int] | scala.Boolean`")
  }

  test("AnyRef") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.AnyRef) {
      List(A1(1), B(2))
    }
    assertError(result)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )
  }

  test("Matchable") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Matchable) {
      List("a", true)
    }
    assertError(result)("least upper bound is `scala.Matchable`. `java.lang.String | scala.Boolean`")
  }

  test("Product") {
    val mustError1 = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(A1(1), B(2))
    }
    assertError(mustError1)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )

    val mustError2 = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(Right[Int, Int](1), Option("a"))
    }
    assertError(mustError2)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `scala.util.Right[scala.Int, scala.Int] | scala.Option[java.lang.String]`"
    )

    val mustEmpty = WartTestTraverser(OrTypeLeastUpperBound.Product) {
      List(A1(1), A2(2))
    }
    assertEmpty(mustEmpty)
  }

  test("Serializable") {
    val result = WartTestTraverser(OrTypeLeastUpperBound.Serializable) {
      List(A1(1), B(2))
    }
    assertError(result)(
      "least upper bound is `java.lang.Object & scala.Product & java.io.Serializable`. `org.wartremover.test.OrTypeLeastUpperBoundTest.A1 | org.wartremover.test.OrTypeLeastUpperBoundTest.B`"
    )
  }
}

object OrTypeLeastUpperBoundTest {
  sealed abstract class A
  case class A1(x: Int) extends A
  case class A2(x: Int) extends A

  case class B(x: Int)

  sealed abstract class E[+A, +B] extends Product with Serializable
  final case class L[+A](a: A) extends E[A, Nothing]
  final case class R[+B](b: B) extends E[Nothing, B]
}