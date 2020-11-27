package com.catstudy.recap.part3

object Evaluations {
  // Evaluation - mechanism by which an expression is reduced to a value

  /**
    * Evaluations:
    * 1. eager evaluations (default)
    * 2. lazy evaluations with recomputing
    * 3. lazy evaluations and returning the same val each time (memoizing)
    */
  import cats.Eval

  // 1. eager eval
  val instantEval: Eval[Int] = Eval.now {
    println("computing now")
    234
  }

  // 2. lazy with recomputing
  val redoEval: Eval[Int] = Eval.always {
    println("computing again")
    6542
  }

  // 3. lazy memoizing
  val memoizingEval: Eval[Int] = Eval.later {
    println("computing once only")
    5432
  }

  // But what is the practical use of Eval?
  // You can compose purely with flatMap and map

  val composedEval: Eval[Int] = for {
    val1 <- instantEval
    val2 <- redoEval
    val3 <- memoizingEval
  } yield val1 + val2 + val3

  // TODO 1: implement defer in a way it does not run the side effects
  def defer[T](eval: => Eval[T]): Eval[T] = Eval.later(eval.value)

  // "remember" capacity
  val dontRecompute = redoEval.memoize // this will hold the internal value of the redoEval

  // TODO 2: rewrite with Eval
  def reverseList[T](list: List[T]): List[T] =
    if (list.isEmpty) list
    else reverseList(list.tail) :+ list.head

  // non stack safe!!!
  def reverseWithEval[T](list: List[T]): Eval[List[T]] =
    if (list.isEmpty) Eval.now(list)
    else Eval.defer(reverseWithEval(list.tail).map(_ :+ list.head))

  def main(args: Array[String]): Unit = {
    println(instantEval.value) // getting the value from inside Eval

    redoEval.value // "computing again" printed
    redoEval.value // "computing again" printed

    memoizingEval.value // "computing once only" printed
    memoizingEval.value // not printing anything, value already exists and will be reused

    println(composedEval.value)
    //computing now
    //computing again
    //computing once only

    println(composedEval.value)
    // computing again

    println(dontRecompute.value) // computing again
    println(dontRecompute.value) // nothing printed. Memoized

    // TODO 1 solution test
    defer(Eval.now {
      println("hi")
      1234
    })
  }

  // TODO 2 solution test
  println(reverseWithEval((1 to 19999).toList).value)

}
