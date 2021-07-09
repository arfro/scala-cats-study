package com.catstudy.recap.part5

object Kleislis {

  /*
  Kleisli - generic data structure that helps with composing functions returning wrapper instances
  Identical to Reader!
   */

  val func1: Int => Option[String] = x => if (x % 2 == 0) Some("x even") else None
  val func2: Int => Option[Int] = x => Some(x * 4)

  // we want to compose them now ^ to be able to say: func3 = func2 andThen func1
  val plainFunc1: Int => String = x => if (x % 2 == 0) "x even" else "fail"
  val plainFunc2: Int => Int = x => x * 4
  val plainFunc3 = plainFunc2 andThen plainFunc1

  //...but func1 and func2 cant be composed as is. When we want to chain functions with wrapper types we can use Kleisli:
  import cats.data.Kleisli
  import cats.instances.option._ // FlatMap[Option]

  val func1Kleisli: Kleisli[Option, Int, String] = Kleisli(func1) // Kleisli instance now
  val func2Kleisli: Kleisli[Option, Int, Int]  = Kleisli(func2)
  val func3Kleisli: Kleisli[Option, Int, String]  = func2Kleisli andThen func1Kleisli // intuition: this looks like flatmap

  // convenience API
  val multiply: Kleisli[Option, Int, Int] = func2Kleisli.map(_ * 5)
  val chain = func2Kleisli.flatMap(x => func1Kleisli)

  // TODO:
  import cats.Id
  type InterestingKleisli[A, B] = Kleisli[Id, A, B] // wrapper over A => Id[B]
  // hint:
  val times2 = Kleisli[Id, Int, Int](x => x * 2)
  val plus4 = Kleisli[Id, Int, Int](x => x + 4)
  val composed = times2.flatMap(t2 => plus4.map(p4 => t2 + p4))
  val composedFor = for {
    t2 <- times2
    p4 <- plus4
  } yield t2 + p4

  // That looks like a Reader Monad / Dependency Injection!

  def main(args: Array[String]): Unit = {
  }

}
