package com.catstudy.recap.part2

import com.catstudy.recap.part2.CustomMonads.IdentityMonad

import scala.annotation.tailrec

object CustomMonads {

  // it generally boils down to: pure and flatmap implementations

  import cats.Monad
  implicit object OptionMonad extends Monad[Option] {
    // now i need pure and flatmap method (also map can be defined using defs of flatmap and pure)
    override def pure[A](x: A): Option[A] = Option(x)
    override def flatMap[A, B](fa: Option[A])(f: A => Option[B]): Option[B] =
      fa.flatMap(f)
    // tailrecM needed! why? monad represents a SEQUENTIAL computation. Looping function.
    // In order to be a monad a type CANNOT stack overflow. thats why we need tailRecM
    @tailrec
    override def tailRecM[A, B](a: A)(f: A => Option[Either[A, B]]): Option[B] =
      f(a) match {
        case None           => None
        case Some(Left(v))  => tailRecM(v)(f)
        case Some(Right(v)) => Some(v)
      } // that being said... you probably won't use it all that much
  }

  // TODO 1: define monad for the identity type
  type Identity[T] = T
  val aNumber: Identity[Int] = 42

  implicit object IdentityMonad extends Monad[Identity] {
    override def pure[A](a: A): Identity[A] = a
    override def flatMap[A, B](a: A)(f: A => Identity[B]): Identity[B] = f(a)
    @tailrec
    override def tailRecM[A, B]( // this makes little sense for identity monad, but is required for a custom monad
      a: A
    )(f: A => Identity[Either[A, B]]): Identity[B] =
      f(a) match {
        case Left(v)  => tailRecM(v)(f)
        case Right(v) => v
      }
  }

  // harder example
  sealed trait Tree[+A]
  final case class Leaf[+A](leaf: A) extends Tree[A]
  final case class Branch[+A](left: Tree[A], right: Tree[A]) extends Tree[A]

  // TODO 2: define a monad for this tree. For easy: no need to do tailrec

  implicit object TreeMonad extends Monad[Tree] {
    override def pure[A](a: A): Tree[A] = Leaf(a)
    override def flatMap[A, B](a: Tree[A])(f: A => Tree[B]): Tree[B] = {
      a match {
        case Leaf(a)             => f(a)
        case Branch(left, right) => Branch(flatMap(left)(f), flatMap(right)(f))
      }
    }
    // ^ but the above is stack recursive!! not stack safe so not a good monad then :(
    // this is still not tail recursive, its very difficult. Not directly related to cats, so i will skip.
    override def tailRecM[A, B](a: A)(f: A => Tree[Either[A, B]]) = {
      def stackRec(t: Tree[Either[A, B]]): Tree[B] =
        t match {
          case Leaf(Left(a))       => stackRec(f(a))
          case Leaf(Right(a))      => Leaf(a)
          case Branch(left, right) => Branch(stackRec(left), stackRec(right))
        }

      stackRec(f(a))

    }
  }

}
