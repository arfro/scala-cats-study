package com.catstudy.recap.part3

object Writers {

  // Writer is a data type in cats that let you keep track of useful info while your data is being manipulated
  // It is very hand to keep a sequence of manipulations in a purely functional way. Like logs, something you might want to dump into a file.
  // You define a write, manipulate with pure FP and then dump the logs or the value

  // Benefits of writers:
  // #1. Purely functional way to get logs or avoid printing
  // #2. We can get logs separately for each writer and process logs independently as we see fit whereas with printing the logs are all combined and you can't tell which log belongs to which thread

  import cats.data.Writer
  // Int is our actual outcome value and list[String] is the track keeping value (logs)
  val aWriter: Writer[List[String], Int] = Writer(List("something"), 45)
  val increaseWriter = aWriter.map(_ * 2) // value increases, but logs stay the same
  // let's modify logs then:
  val aLogsWriter = aWriter.mapWritten(_ :+ "found something") // on change to Int value this will be added
  // we might want to modify both at the same time, bimap!!
  val bothWriter = aWriter.bimap(_ :+ "found something in bimap", _ + 1)
  // we can also do mapBoth, here you can include the value in the logs!!
  val bothWriter2 = aWriter.mapBoth { (logs, value) =>
    (logs :+ "something interesting bothWriter2", value + 3)
  }

  // ..at the very end you jsut do:
  val desiredValue = aWriter.value // to get just value
  val logs = aWriter.written // to get just logs
  val (log, value) = aWriter.run // get both at once

  val writerA = Writer(Vector("a", "b"), 10)
  val writerB = Writer(Vector("c", "d"), 30)
  // reminder: semigroup needed here to be able to combine the logs "natually" that is for the list, append all elements of it
  import cats.instances.vector._
  val compositeWriter = for {
    va <- writerA
    vb <- writerB
  } yield va + vb

  // reset the logs in writer
  // we need a monoid (that has a zero value for a List (log type))
  import cats.instances.list._
  val emptyWriter = aWriter.reset // clear the logs BUT KEEP THE VALUE

  // exercise 1. re-write the function that prints something with writers, in a purely functional way
  def countAndSay(n: Int): Unit = {
    if (n <= 0) println("starting")
    else {
      countAndSay(n - 1)
      println(n)
    }
  }
// first try and this solution goes backwards: "10 9 8 7 6 5 4 3 2 1 starting"
  def countAndLog(n: Int): Writer[Vector[String], Int] =
    if (n <= 0) Writer(Vector("starting"), n)
    else {
      Writer(Vector(n.toString), n)
        .flatMap(nr => countAndLog(nr - 1))
    }

  // this goes normal
  def countAndLog2(n: Int): Writer[Vector[String], Int] =
    if (n <= 0) Writer(Vector("starting"), n)
    else {
      countAndLog2(n - 1).bimap(_ :+ n.toString, _ => n)
    }

  // TODO: re-write this with writers
  def naiveSum(n: Int): Int = {
    if (n <= 0) 0
    else {
      println(s"Now at $n")
      val lowerSum = naiveSum(n - 1)
      println(s"computer sum of $n - ${n - 1} = $lowerSum")
      lowerSum + n
    }
  }

  // my first try:
  def sumWithWriters(n: Int): Writer[Vector[String], Int] = {
    if (n <= 0)
      Writer(Vector(), 0)
    else {
      sumWithWriters(n - 1).flatMap(
        _ =>
          Writer(
            Vector(
              s"Now at $n",
              s"computed: sum($n + ${n - 1}) = ${n + (n - 1)})"
            ),
            n
        )
      )
    }
  }

  // better way:
  def sumWithWriters2(n: Int): Writer[Vector[String], Int] = {
    if (n <= 0)
      Writer(Vector(), 0)
    else {
      for {
        _ <- Writer(Vector(s"Now at $n"), n)
        lowerSum <- sumWithWriters2(n - 1)
        _ <- Writer(Vector(s"computed: sum(${n - 1}) = $lowerSum)"), n)
      } yield lowerSum + n
    }
  }

  def main(args: Array[String]): Unit = {
    println(compositeWriter.run)
    countAndSay(10)
    println(countAndLog(10))
    countAndLog2(10).written.foreach(println)
    naiveSum(10)
    sumWithWriters(10).written.foreach(println)
    sumWithWriters2(10).written.foreach(println)
    // note: with futures, the writer logs wont interfere with one another! we can process all logs from future 1 vs future 2 separately.
  }
}
