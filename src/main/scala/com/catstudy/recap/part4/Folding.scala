package com.catstudy.recap.part4

import cats.{Eval, Monoid}

object Folding {

  // TODO: in terms of fold left /rgiht
  object ListExcercises {
    def map[A, B](list: List[A])(f: A => B): List[B] =
      list.foldRight(List.empty[B])((a, currentList) => f(a) :: currentList)

    def flatMap[A, B](list: List[A])(f: A => List[B]): List[B] =
      list.foldLeft(List.empty[B])((currentList, a) => currentList ++ f(a))

    def filter[A](list: List[A])(predicate: A => Boolean): List[A] =
      list.foldRight(List.empty[A])((a, currentList) => if (predicate(a)) a :: currentList else currentList)

    def combineAll[A](list: List[A])(implicit monoid: Monoid[A]): A =
      list.foldLeft(monoid.empty)(monoid.combine)

    import cats.Foldable
    import cats.instances.list._
    val sum = Foldable[List].foldLeft(List(1,2,3,4), 0)(_ + _) // works exactly the same, just different API - works same for Vectors etc.

    import cats.instances.option._ // Foldable[Option]
    val sumOption = Foldable[Option].foldLeft(Option(4), 39)(_ + _) // generalisible APIs

    // Then whats the benefit?
    // - fold right is stack recusion - using eval makes everything stack safe!
  val sumRight = Foldable[List].foldRight(List(1,2,3,4), Eval.now(0)) { (num, eval) =>
    eval.map(_ + num)
  }

    def main(args: Array[String]): Unit = {

    }

  }
}
