package com.catstudy.recap.part2

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

object Monads {

  // list
  val nrs = List(1, 2, 3)
  val carsList = List("a", "b", "c")

  // exercise 1.1 how do you create all the combinations of the (nr, car) list?
  val result1 = nrs.flatMap(nr => carsList.map((nr, _)))

  // option
  val nr = Option(4)
  val char = Option('3')

  // exercise 1.2 - how to combine options?
  val result2 = nr.flatMap(nr => char.map((nr, _)))

  // futures
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(1))

  val futNr = Future(23)
  val futChar = Future('g')

  // exercise 1.3 - combine futures
  val result3 = futChar.flatMap(char => futNr.map((_, char)))

  /*
  ^ above we can see that we can combine three types that have nothing to do with each other in the same manner.
  There is two things we need to be able to cmobine things like above:
  1. wrapping a value into a M value (e.g. list, future, option)
  2. we need a bind (flatMap mechanism)

  ^ a type that embodies the above is called a Monad
   */

  // FlatMap is NOT an iteration!!! it's a general transformation pattern!
  // however flatMap does guarantee a sequential execution

  // Monads are higher kinded types.

  trait MyMonad[M[_]] { // takes a type argument which itself takes a type argument
    def pure[A](value: A): M[A] // 1. wrap a value in MyMonad
    def flatMap[A, B](ma: M[A])(fctn: A => M[B]): M[B] // 2. bind
    // exercise 3: implement map method in MyMonad in terms of pure and flatMap.
    // if we have pure and flatMap we can always have map!
    // that means that monad EXTENDS a functor, because it can provide map. This we can also see in the cats library: Monad extends Functor (not directly)
    def map[A, B](ma: M[A])(f: A => B): M[B] =
      flatMap(ma)(a => pure(f(a)))
  }

  import cats.Monad
  import cats.instances.option._
  val optionMonad = Monad[Option] // instance of Monad[Option]
  val anOption = optionMonad.pure(5) // returns Option(5)
  val combinedOption = optionMonad.flatMap(anOption)(int => Some(int.toString))

  import cats.instances.list._
  val listMonad = Monad[List]
  val aList = listMonad.pure(3) // here pure takes a single value and wraps it in List -- List(3)
  val combinedList = listMonad.flatMap(aList)(nr => List(nr, nr + 1)) // List(4,5)

  // exercise 2 - use monad of a Future
  import cats.instances.future._
  val futureMonad = Monad[Future]
  val aFuture = futureMonad.pure(100)
  val combinedFuture = aFuture.flatMap(int => Future(int + 10))

  // specialized API
  def getPairsList(numbers: List[Int], chars: List[Char]): List[(Int, Char)] =
    numbers.flatMap(nr => chars.map(char => (nr, char)))

  // if i wnated to support Options or Futures above ^ not only List... it's enough if you change "List" to "Option". The rest is identical!
  def getPairsOption(numbers: Option[Int],
                     chars: Option[Char]): Option[(Int, Char)] =
    numbers.flatMap(nr => chars.map(char => (nr, char)))

  // .. and for the future exactly the same, again duplicating.
  def getPairsFuture(numbers: Future[Int],
                     chars: Future[Char]): Future[(Int, Char)] =
    numbers.flatMap(nr => chars.map(char => (nr, char)))

  // Monads let you generalise the above three defs.
  def getPairs[M[_], A, B](ma: M[A],
                           mb: M[B])(implicit monad: Monad[M]): M[(A, B)] = {
    monad.flatMap(ma)(a => monad.map(mb)(b => (a, b)))
  }

  // Now we can get pairs of any data structure which has an implicit monad M in scope

  // MONAD --- part 2
  // Extension methods: weird imports!
  // extension methods: pure, flatMap. they are part of two different imports
  import cats.syntax.applicative._ // kinda like weaker monad: this import contains pure
  val oneOpt = 1.pure[Option] // Option(1). Explicit Monad[Option] wraps value in Option.
  val oneFut = "lala".pure[Future] // Future.successful("lala"). Wraps value in Future
  // for flatMap:
  import cats.syntax.flatMap._ // flatMaps here
  // it doesn't highlight now (is not used) because Option type has its own flatMap available.
  // This import however comes in handy when working with my own custom monad implementations
  val optTransformed = oneOpt.flatMap(x => (x + 1).pure[Option])

  // exercise 3: implement map method in MyMonad
  // takewaay from exercise: monad extends functor (also represented in cats library)
  // that means that if we import functors syntax we will be able to use .map
  import cats.syntax.functor._ // map is here
  val optMapped = oneOpt.map(x => (x + 8).pure[Option])

  // exercise 4: implement a shorter version of getPairs using for comprehensions:
  def getPairsShorter[M[_], A, B](
    ma: M[A],
    mb: M[B]
  )( // we can improve by removing implicit monad and say that M[_] needs to have Monad in scope...
    implicit monad: Monad[M]): M[(A, B)] = {
    for {
      a <- ma
      b <- mb
    } yield (a, b)
  }

  def getPairsShorterWithoutImplicit[M[_]: Monad, A, B]( // M[_]: Monad <- that means that M[_] needs to have a Monad in scope
                                                        ma: M[A],
                                                        mb: M[B]): M[(A, B)] = {
    for {
      a <- ma
      b <- mb
    } yield (a, b)
  }

  def main(args: Array[String]): Unit = {
    // exercise 1.1
    println(result1)
    // exercise 1.2
    println(result2)
    // exercise 1.3
    println(result3)
    // exercise 2
    combinedFuture.onComplete(int => println(int.get))
    // def getPairs is so freaking powerful!!!
    println(getPairs(nrs, carsList)) // we have listMonad in scope (import cats.instances.list._)
    getPairs(Future(2), Future("abc"))
      .foreach(println(_)) // we have futureMonad in scope (import cats.instances.future._)
    println(getPairs(Option(2), Option("abc"))) // we have optionMonad in scope (import cats.instances.option._)
    // exercise 4:
    getPairsShorter(Future(4), Future(11)).foreach(println(_))
  }
  // CLASS RECAP:
  // Monad: powerful type class that provides two methods:
  // * pure (wrap value into Monad)
  // * flatMap (transforms monadic values in sequence)
  //
  // import cats.Monad contains the Monad type class
  // import cats.instances.list._ contains the List type class instances (inc. Monad)
  //
  // .map can be created from .pure and .flatMap and so monads are extending functors
  //
  // Use cases:
  // * anything sequential (e.g. transformations, async chained computations, dependant computations, list combinations)
  //
  // Again, for comprehensions are NOT iterations!
  //
  // FlatMap is a mental model of chained transformations
  //
}
