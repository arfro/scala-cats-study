package com.catstudy.recap.part2

import scala.util.Try

object Functors {

  // First Higher Kinded Type! Yay
  // Functor is a type class that generalises the idea of .map

  trait MyFunctor[F[_]] { // F[_] "higher kinded type"
    def map[A, B](initialVal: F[A])(f: A => B): F[B]
  }

  import cats.Functor // type class def
  import cats.instances.list._ // type class instance
  val listFunctor = Functor[List]
  val incrementedNrs = listFunctor.map(List(12, 3, 4))(_ + 1)
  import cats.instances.option._
  val functorOption = Functor[Option]
  val incremenetedOption = functorOption.map(Option(1))(_ + 1)

  import cats.instances.try_._
  val functorTry = Functor[Try].map(Try(24))(_ + 1)

  // that looks so complicated. Why not just use the .map on collections directly?
  // It becomes more useful when we try to generalise a transformation!

  def do10xList(list: List[Int]): List[Int] = list.map(_ * 10)
  def do10xOption(opt: Option[Int]): Option[Int] = opt.map(_ * 10)
  def do10xTry(attempt: Try[Int]): Try[Int] = attempt.map(_ * 10)

  // ^ that's a ton of functions! Could we not just wrap it together somehow?

  def do10xGeneral[F[_]](
    container: F[Int]
  )(implicit functor: Functor[F]): F[Int] = {
    // how to enforce tht F[Int] must have map method? There must be a Functor[F] in scope!
    functor.map(container)(_ * 10)
  }

  // EXERCISE 1: add own functor for a binary tree
  // hint: define an object which extends Functor[Tree]
  trait Tree[+T]
  object Tree { // smart constructors
    def leaf[T](value: T): Tree[T] = Leaf(value)
    def branch[T](value: T, left: Tree[T], right: Tree[T]) =
      Branch(value, left, right)
  }
  case class Leaf[+T](value: T) extends Tree[T]
  case class Branch[+T](value: T, left: Tree[T], right: Tree[T]) extends Tree[T]

  implicit val treeFunctor: Functor[Tree] = new Functor[Tree] {
    override def map[A, B](fa: Tree[A])(f: A => B): Tree[B] = {
      fa match {
        case Leaf(value) => Leaf(f(value))
        case Branch(value, left, right) =>
          Branch(f(value), map(left)(f), map(right)(f))
      }
    }
  }

  // extension method for Functor: .map
  import cats.syntax.functor._

  val tree: Tree[Int] =
    Tree.branch(23, Tree.leaf(4), Tree.branch(3, Tree.leaf(4), Tree.leaf(9)))

  val incrementedTree = tree.map(_ * 2) // and no now we can call .map on Tree !!!

  // EXERCISE 2: write a short do10x method using extension methods
  // [F[_]: Functor] <- context bound restriction:
  // "there must be an implicit Functor[F[_]] in scope", BUT we could be passing implicit val as curried arg as usual. This just looks much nicer
  def do10xGeneralShorter[F[_]: Functor](container: F[Int]): F[Int] = {
    container.map(_ * 10)
  }

  def main(args: Array[String]): Unit = {
    println(do10xGeneral(List(1, 2, 3, 4, 5)))
    println(do10xGeneral(Option(3)))

    // EXERCISE 1 TEST:
    val tree: Tree[Int] = Branch(13, Leaf(3), Branch(5, Leaf(62), Leaf(6)))
    val tree2NoType = Branch(13, Leaf(3), Branch(5, Leaf(62), Leaf(6)))
    val tree2NoTypeWithSmartConstruct =
      Tree.branch(13, Tree.leaf(3), Tree.branch(3, Tree.leaf(62), Tree.leaf(6)))
    println(do10xGeneral(tree))
    //println(do10xGeneral[Tree[Int]](tree2NoType))
    //println(do10xGeneral[Tree[Int]](tree2NoTypeWithSmartConstruct))

    // EXERCISE 2 TEST:

    println(do10xGeneralShorter(List(1, 3, 4, 5)))

  }
}
