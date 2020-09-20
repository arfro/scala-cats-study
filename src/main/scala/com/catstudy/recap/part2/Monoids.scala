package com.catstudy.recap.part2

object Monoids {

  // Main takeaway: ---> !!! Monoid is a semigroup just that it provides us with a "zero" (a.k.a. "empty") value. !!! <---

  // Monoids solve something that semigroups lack...
  // Semigroup's combine is always associative, that means: 4 + 6 == 6 + 4. It doesn't matter which nr we start with.
  // Semigroup does not provide a starting value for a fold.
  // Semigroup is therefor NOT ENOUGH to be able to fold generic types, it's missing the "zero" equivalent.
  // Under the hood monoids actually extends semigroups

  import cats.Semigroup
  import cats.instances.int._ // this is shared for all type classes: monoid, semigroup etc.
  import cats.syntax.semigroup._ // import |+| syntax for combining

  val nrs = (1 to 1000).toList
  val sumLeft = nrs.foldLeft(0)(_ |+| _)
  val sumRight = nrs.foldRight(0)(_ |+| _)

  // general API
  def combineFold1[T](
    list: List[T]
  )(implicit semigroup: Semigroup[T]) /*: T*/ = {
    // what is the starting value? What is an equivalent of "zero" in all types?
    // list.foldLeft( /* how do we know what to put here? */ )(_ |+| _) // |+| not found error because we don't know the starting value
  }

  // We need to expand Semigroup into another type class that will give us a "zero" value. That type class is called a MONOID.
  import cats.Monoid
  val intMonoid = Monoid[Int]
  val combineInt = intMonoid.combine(34, 64) // 98
  val zero = intMonoid.empty // intuitive "zero" value for a type.
  import cats.instances.string._
  val strMonoid = Monoid[String].empty // ""
  import cats.instances.list._
  val listMonoid = Monoid[List[Int]].empty // List()
  import cats.instances.option._
  val optMonoid = Monoid[Option[Int]].empty // None
  val combineOpt = Monoid[Option[Int]].combine(Option(3), Option.empty[Int]) //  Some(3)
  val combineOpt2 = Monoid[Option[Int]].combine(Option(3), Option(6)) //  Some(9)

  // extension method for monoids - same extension method: |+|
  // import cats.syntax.monoid._ // here i could also use the already present above cats.syntax.semigroup._ because monoid is a semigroup!
  val combineOptFancy = Option(3) |+| Option(6) // Some(9)

  // Exercise 1. Implement a reduce by fold using monoids
  def combineFold[T](list: List[T])(implicit monoid: Monoid[T]): T = {
    list.foldLeft(monoid.empty)(_ |+| _)
  }

  // Exercise 2. Combine a list of phonebooks as Maps[String, Int]
  val phoneBooks = List(
    Map("alice" -> 1234, "bob" -> 12345),
    Map("charlie" -> 8765, "daniel" -> 76543),
    Map("tina" -> 323626)
  )

  def combinePhoneBooks[T, K](
    list: List[Map[T, K]]
  )(implicit monoid: Monoid[Map[T, K]]): Map[T, K] = {
    list.foldLeft(monoid.empty)(_ |+| _)
    // this is exactly same as above so i can just import Map instances and the whole "combinePhoneBooks" is then redundant!
  }

  // Exercise 3. Multi tab combining from a shop cart
  // hint: i can use combineFold method that i have already!
  // hint 2: to define a monoid: Monoid.instance
  case class ShoppingCart(items: List[String], total: Double)
  import cats.instances.double._
  implicit val shoppingCartMonoid =
    Monoid.instance[ShoppingCart](
      ShoppingCart(List.empty, 0),
      (sc, sc2) => ShoppingCart(sc.items |+| sc2.items, sc.total |+| sc2.total)
    )
  def checkout(carts: List[ShoppingCart]): ShoppingCart = combineFold(carts)

  def main(args: Array[String]): Unit = {
    println(sumLeft == sumRight) // this is identical because Semigroup's combine is associative
    println(optMonoid) // just testing if i was right. Yup - it's a None

    // Test Exercise 1.
    println(combineFold(nrs)) //500500
    println(combineFold(List("I ", "like ", "monoids")))

    // Test Exercise 2.
    import cats.instances.map._
    println(combinePhoneBooks(phoneBooks))
    // But! because i have a "combineFold" already above i don't have to do this at all. I can just reuse "combineFold"!
    println(combineFold(phoneBooks))

    // Test Exercise 3.
    import Monoids.shoppingCartMonoid
    val carts = List(
      ShoppingCart(List("name", "lala"), 24.2),
      ShoppingCart(List("lala", "hey"), 77.3)
    )
    println(checkout(carts))
  }
}
