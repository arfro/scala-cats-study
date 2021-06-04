package com.catstudy.recap.part4

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object Semigroupals {

  /**
   * Semigroupal: HKT
   *
   * you don't combine two values but you tuple them and then you can decide how you want to combine the values
   * thats why it's 'semigroupal' and not 'semigroup'
   * Combining without a need to unwrap and wrap back
   */

  trait MySemigroupal[F[_]] {
    def product[A, B](fa: F[A], fb: F[B]): F[(A, B)]
  }

  import cats.Semigroupal

  import cats.instances.option._ // Semigroupal[Option]
  val optionSemigroupal = Semigroupal[Option]
  val someOption = optionSemigroupal.product(Some(1234), Some("hello")) // Some(1234, "hello")
  val noneOption = optionSemigroupal.product(None, Some(1)) // None

import cats.instances.future._ // Semigroupal[Future]
  implicit  val executionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  val tupledFuture = Semigroupal[Future].product(Future(1), Future("hello")) // Future((1, "hello"))

  // Why are semigroupals useful?
  import cats.instances.list._
  val listTuple = Semigroupal[List].product(List(1,2), List("a", "b")) // cartesian product between two lists!

  // exercise: implement product with monads - the same can be done using flatmap and map - so using a monad. Which means that monads are semigroupals
  import cats.Monad

  def productWithMonads[F[_], A, B](fa: F[A], fb: F[B])(implicit monad: Monad[F]): F[(A, B)] = {
    monad.flatMap(fa)(a => monad.map(fb)(b => (a, b)))
  }

  // and now clearer with extension methods...
  import cats.syntax.functor._
  import cats.syntax.flatMap._
  def nicerProductWithMonads[F[_], A, B](fa: F[A], fb: F[B])(implicit monad: Monad[F]): F[(A, B)] = {
    for {
      a <- fa
      b <- fb
    } yield (a, b)
  }

  // sooo.. why semigroupals are useful at all if we can do all of this with a monad?
  // use case: Validated. Combining instances without needing to follow monad laws

  import cats.data.Validated
  type ErrorOr[T] = Validated[List[String], T]
  val validatedSemigroupal = Semigroupal[ErrorOr] // requires implicit semigroup of list Semigroup[List[_]]

  val invalidsCombination = validatedSemigroupal.product(
    Validated.invalid(List("boom", "boom2")),
    Validated.invalid(List("can't be"))
  )

  type EitherErrorOr[T] = Either[List[String], T]
  import cats.instances.either._
  val eitherSemigroupal = Semigroupal[EitherErrorOr]
  val eitherCombination = eitherSemigroupal.product(
    Left(List("boom")),
    Left(List("oh no", "oops"))
  )

  // semigroupal is useful for where we don't want to short circuit
val zipListSemigroupal: Semigroupal[List] = new Semigroupal[List] {
  override def product[A, B](lista: List[A], listb: List[B]): List[(A, B)] = {
    lista.zip(listb)
  }
}

  // exercise: define a Semigroupal[List] which does a zip



  def main(args: Array[String]): Unit = {
    println(listTuple) // cartesian product between two lists!
    println(productWithMonads(List(1,2), List("a", "b")))
    println(nicerProductWithMonads(List(1,2), List("a", "b")))
    println(invalidsCombination) // all combined to a bigger list
    println(eitherCombination) // the second left errors are missing - because monad short circuits on left.
    println(zipListSemigroupal.product(List(1,2), List("a", "b"))) // unlike the first example - now we have a zip
  }

}
