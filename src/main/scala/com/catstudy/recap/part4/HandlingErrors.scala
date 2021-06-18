package com.catstudy.recap.part4

import cats.Monad

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object HandlingErrors {

  trait MyMonadError[M[_], E] extends Monad[M] {
    def raiseError[A](e: E): M[A]
  }

  import cats.MonadError
  import cats.instances.either._

  type ErrorOr[A] = Either[String, A]
  val monadErrorEither = MonadError[ErrorOr, String]
  val success = monadErrorEither.pure(23) // Either[Sting, Int] = Right(32)
  val failure = monadErrorEither.raiseError[Int]("something wrong") // Either[ String, Int] == Left("something wrong")
  val handledError: ErrorOr[Int] = monadErrorEither.handleError(failure) {
    case "bad" => 44
    case _ => 89
  }
  // similar... recover (to concrete type) or recoverWith (to another wrapper type)
  val handleError2: ErrorOr[Int] = monadErrorEither.handleErrorWith(failure){
    case "bad" => monadErrorEither.pure(444)
    case _ => Left("something else")
  }

  val filteredSuccess = monadErrorEither.ensure(success)("nr too small")(_ > 100) // clunky!

  // Try and Future instances
  import cats.instances.try_._ // implicit MonadError[Try], E = Throwable
  val exception = new RuntimeException("very bad")
  val pureException: Try[Int] = MonadError[Try[Int], Throwable].raiseError(exception) // purely functional!

  import cats.instances.future._
  implicit val ec = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  MonadError[Future, Throwable].raiseError((exception))

  def main(args: Array[String]): Unit = {
    ()
  }

}
