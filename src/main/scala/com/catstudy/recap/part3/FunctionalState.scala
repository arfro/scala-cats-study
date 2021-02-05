package com.catstudy.recap.part3

object FunctionalState {

  // As we are looking at functional programming world we don't mutate things. We need to keep track of state changes instead.
  // in Cats state means data structure that defines evolution of the system

  // from state S we get another state of type S AND a result of the computation
  type MyState[S, A] = S => (S, A)

  // in cats State wraps a single function:
  import cats.data.State
  val countAndSay: State[Int, String] = State(currentCount => (currentCount + 1, s"counted $currentCount"))
  // countAndSay.run(10) returns Eval, so we need to get .value
  val (eleven, countedTen) = countAndSay.run(10).value

  // why so complicated? !!! state data structures can be composed so we can purely functionally do iterative computation !!!

  // bad scala code watch out!! :)
  var a = 10
  a += 1
  val firstComputation = s"Added 1 + 10, got $a"
  a *= 5
  val secondComputation = s"Multiplied with 5, got $a"

  // above can be expressed purely:
  val firstTransformation = State((s: Int) => (s + 1, s"Added 1 + 10, got ${s + 1}"))
  val secondTransformation = State((s: Int) => (s * 5, s"Multiplied with 5, got ${s * 5}"))
  // because of flatMapping we can iteratively compute final value of a without mutating. "Combining" happens automatically
  val compositeTransformation: State[Int, (String, String)] = firstTransformation.flatMap{
    firstResult => // s + 1 = 11
      secondTransformation.map(
        secondResult => // 11 * 5 = 55
          (firstResult, secondResult)
      )
  }

  //.. or as a for comprehension
  val forComp = for {
    firstResult <- firstTransformation
    secondResult <- secondTransformation
  } yield (firstResult, secondResult)

  // ...but why can't we just sequence functions if State is a wrapper over functions?
  // Let's see how chaining two functions would look like:
  val func1 = (s: Int) => (s + 1, s"Added 1 + 10, got ${s + 1}")
  val func2 = (s: Int) => (s * 5, s"Multiplied with 5, got ${s * 5}")
  val compositeFunc = func1 andThen {
    case (newState, firstResult) => (firstResult, func2(newState))
  }
  // Answer to the above question:
  // because final result 55 will be deeply nested inside tuples (see "println(compositeFunc(10))" output). The more functions we have the deeper final result will be

  // TODO: an online store where we sell guitars.
  case class ShoppingCart(items: List[String], total: Int)
  // this below method represents a transition from a shopping cart to ANOTHER shopping cart and the total amount at that point
  def addToCart(item: String, price: Int): State[ShoppingCart, Double] = State(
    (s: ShoppingCart) =>
      (ShoppingCart(s.items :+ item, s.total + price), s.total + price)
  )

  val annetteCart: State[ShoppingCart, Double] = for {
    _ <- addToCart("guitar", 300) // this looks like side effects!
    _ <- addToCart("strings", 40) // this looks like side effects!
    total <- addToCart("electric cable", 10)
  } yield total

  // TODO 2: pure mental gymnastics
  // return state structure that, when run, will not change the state but will issue the value f(a)
  def inspect[A, B](f: A => B): State[A, B] = State {
    a: A => (a, f(a))
  }
  // return state structure that, when run, returns the value of that state and make no changes
  def get[A]: State[A, A] = State {
    a: A => (a, a)
  }
  // return state structure that, when run, returns Unit and sets the state to that value
  def set[A](value: A): State[A, Unit] = State {
    a: A => (value, ())
  }
  // return state structure that, when run, returns Unit and run f(state)
  def modify[A](f: A => A): State[A, Unit] = State {
    a: A => (f(a), ())
  }

  // all of the above ^ are already in the cats State companion object!
  import cats.data.State._

  // completely functional way! iterative
  val program: State[Int, (Int, Int, Int)] = for {
    a <- get[Int] // return initial state
    _ <- set[Int](a + 10) // set state to a + 10
    b <- get[Int] // get the result of the above
    _ <- modify[Int](_ + 13) // modify the above by '+ 13'
    c <- inspect[Int, Int](_ * 3) // inspect and then run '* 3'
  } yield (a, b, c)


  def main(args: Array[String]): Unit = {
    println(compositeTransformation.run(10).value) // (55,(Added 1 + 10, got 11,Multiplied with 5, got 55))
    println(compositeFunc(10)) // (Added 1 + 10, got 11,(55,Multiplied with 5, got 55))
    println(annetteCart.run(ShoppingCart(List(), 0)).value) // (ShoppingCart(List(guitar, strings, electric cable),350),350.0)
  }
}
