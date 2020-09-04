package com.catstudy.recap.part1

object CatsIntro {

  // ---- 1. Eq = type safe equality ----
  // Adds an extension method on all types that automatically check whether types match. Otherwise compile error

  val comparison = 1 == "ad" // valid expression, but it will always be false.

  // Part 1. Type class import
  import cats.Eq

  // Part 2. import Type class instances for the type you need
  import cats.instances.int._

  // Part 3. Use type class API (explicit usage)
  val intEquality = Eq[Int]
  val typeSafeComparison = intEquality.eqv(2, 3)
  // val unsafeComparison = intEquality.eqv(4, "") this won't even compile

  // Part 4. add extension methods (nice, extension method style usage)
  import cats.syntax.eq._ // all extension methods here
  val anotherTypeSafeComp = 2 === 3 // === is an extension on Int in the presence of a type class instance (Part 2.)
  val neqComparison = 5 =!= 3 // true. 5 not equal 3
  // val invalid = 4 === "string" // doesn't compile
  // extension methods are only visible in the presence of the correct type class instance! (part 2)

  // part 5. extending type class operations to composite types
  import cats.instances.list._ // without those implicits it won't work! Eq[List[_]] brought in scope now.
  val listComparison = List(3) === List(5)

  // Part 6. My Type is not supported!!! What about ToyCar?
  case class ToyCar(model: String, price: Double)

  // 1. Create Type class for ToyCar. ToyCars are equal if their models are the same.
  implicit val toyCarEq: Eq[ToyCar] = Eq.instance[ToyCar] { (car1, car2) =>
    car1.model == car2.model
  }

  val compareTwoCars = ToyCar("ferrari", 22) === ToyCar("ferrari", 55) // true!

  def main(args: Array[String]): Unit = {
    println(listComparison)
  }

}
