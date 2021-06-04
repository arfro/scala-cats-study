package com.catstudy.recap.part4

object Applicatives {

  // HKT
  // Applicative = Functors + pure method. Used to create a wrapped value from a raw value
  // Rarely used by themselves because most of the data structures are monadic types. An exception: Validated.
  // Validated does NOT follow monadic laws but you can map and flatMap them. See "Validated" below.

  import cats.Applicative
  import cats.instances.list._
  val listApplicative = Applicative[List]
  val aList = listApplicative.pure(3) // List(3)

  import cats.instances.option._
  val optionApplicative = Applicative[Option]
  val anOption = optionApplicative.pure(4) // Some(4)

  // WHy are they useful?
  // They are functors - so can map over them.

  // pure extension method
  import cats.syntax.applicative._
  val aList2 = 2.pure[List] // List(2)
  val anOption2 = 2.pure[Option] // Some(2)

  // Monads' PURE method comes from monads being Applicative! Monads extend Applicative!
  // Applicatives extend Functors

  // Validated
  import cats.data.Validated
  type ErrorOr[T] = Validated[List[String], T]
  val aValidValue: ErrorOr[Int] = Validated.valid(3) // <-- this looks a lot like "pure"
  val modifiedValidated: ErrorOr[Int] = aValidValue.map(_ + 2) // map

  val validatedApplicative = Applicative[ErrorOr]

  // exercise: thought experiment: can't do the below...!!
  def productWithApplicatives[W[_], A, B](wa: W[A], wb: W[B])(implicit applicative: Applicative[W]): W[(A, B)] = ???
  // ...but what if the below was in scope?
  def ap[W[_], A, B](wf: W[A => B])(wa: W[A]): W[B] = ??? // this has an equivalent in Applicative: 'applicative.ap'. IT's very abstract and quite difficult to understand
  // ...then:
  def productWithApplicatives2[W[_], A, B](wa: W[A], wb: W[B])(implicit applicative: Applicative[W]): W[(A, B)] = { // <- this is super hard, complex problem
    val fucntionWrapper: W[B => (A, B)] = applicative.map(wa)(a => (b: B) => (a, b))
    applicative.ap(fucntionWrapper)(wb)
  }

  def main(args: Array[String]): Unit = {

  }

}
