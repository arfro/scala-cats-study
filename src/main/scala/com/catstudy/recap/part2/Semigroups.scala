package com.catstudy.recap.part2

object Semigroups {

  // COMBINE two elements of the same type: (A, A) => A
  // Combine means something different for different type classes.
  // Used to write very generic functions that can combine ANY type.
  // Usage real life: data crunching, big data processing, timestamp, eventual consistency need to be reconciled
  // but also: when running writer monad and we want to combine e.g. Vector[String] of logs in a for comprehension we will need a Vector semigroup in scope!

  import cats.Semigroup
  import cats.instances.int._

  val naturalIntSemigroup = Semigroup[Int]
  // but what does it mean to combine two ints? intuition says: addition. And that's right!
  val intCombination = naturalIntSemigroup.combine(4, 6)

  import cats.instances.string._
  val naturalStringSemiGroup = Semigroup[String]
  val stringCombination = naturalStringSemiGroup.combine("123", "456")

  // specific API: they don't really need a semigroup! we could just say _ + _ in reduce
  def reduceInts(list: List[Int]) = list.reduce(naturalIntSemigroup.combine)
  def reduceStrings(list: List[String]) =
    list.reduce(naturalStringSemiGroup.combine)

  // instead of the above we can create a generic type reducer that can reduce anything
  // generic API
  def reduceThings[T](list: List[T])(implicit semigroup: Semigroup[T]) =
    // semigroup[T] exposes combine that is used on calling reduce of list
    list.reduce(semigroup.combine)

  // EXERCISE 1: support a new, custom type in semigroup
  // hint: same as custom type in Eq
  case class Expense(id: Long, amount: Double)
  implicit val expenseSemigroup: Semigroup[Expense] =
    Semigroup.instance[Expense](
      (left, right) =>
        Expense(Math.max(left.id, right.id), left.amount + right.amount)
    )

  // Extension method from semigroup - |+| read: "combine"
  import cats.syntax.semigroup._
  val intSum = 2 |+| 5 // |+| requires presence of Semigroup[Int]
  val stringSum = "ada" |+| "lala" // requires presence of Semigroup[String]
  val expenseSum = Expense(1, 53.2) |+| Expense(5, 23.2) // requires presence of Semigroup[Expense]

  // EXERCISE 2: implement reduceThings2
  def reduceThings2[T](list: List[T])(implicit semigroup: Semigroup[T]) =
    list.reduce(_ |+| _)

  def main(args: Array[String]): Unit = {
    println(intCombination) // 10
    println(stringCombination) // 123456
    val list = (1 to 13).toList
    println(reduceInts(list))
    val strList = (1 to 30).toList.map(_.toString)
    println(reduceStrings(strList))

    // general API
    println(reduceThings(List(1, 2, 3, 4)))
    println(reduceThings(List("1", "2", "3", "4")))
    // compiler produces Semigroup[Option[Int]]
    // compiler produces Semigroup[Option[String]]
    import cats.instances.option._
    println(reduceThings(List(Option(3), Option(6), Option.empty[Int]))) // Some(9)!! no need to unwrap Option!
    println(reduceThings(List(Option.empty[String], Option("lalala"))))

    // TEST EXERCISE 1:
    val listExp = List(Expense(1, 44.3), Expense(44, 53.2), Expense(5, 23.19))
    println(reduceThings(listExp))

    // TEST EXERCISE 2:
    val listStr = List("abc", "def")
    println(reduceThings2(listStr))
  }
}
