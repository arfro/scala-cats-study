package com.catstudy.recap.part2

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

object MonadTransformers {

  // Monad transformers: another higher kinded type that act as convinience over NESTED monadic values.
  // They are not monads themselves

  /** OptionT **/
  import cats.data.OptionT
  import cats.instances.list._ // fetch OptionT[List]

  val listOfNrOptions
    : OptionT[List, Int] = OptionT(List(Option(1), Option(3))) // list of option of Int
  val listOfCharOpts: OptionT[List, Char] = OptionT(
    List(Option('a'), Option('b'))
  )
  val listOfTuples: OptionT[List, (Int, Char)] = for {
    char <- listOfCharOpts
    nrs <- listOfNrOptions
  } yield (nrs, char)

  /** EitherT **/
  import cats.data.EitherT
  val listOfEithers: EitherT[List, String, Int] = EitherT(
    List(Left("yo"), Right(5))
  )
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(4))
  val futureOfEither = EitherT.rightT(100)

  // TODO:
  // We have a multi machine cluster for your business whcih will receive a traffic surge following a media appearance.
  // We measure bandwith in units.
  // We want to allocate TWO of our servers to cope with the traffic spike.
  // We know the current capacity for each server and we know we'll hold the traffic if the sum of bandwidth is > 250.

  val bandwidths = Map(
    "server1.rockthe.jvm" -> 50,
    "server2.rockthe.jvm" -> 300,
    "server3.rockthe.jvm" -> 150,
  )

  type AsynResponse[T] = EitherT[Future, String, T]

  import cats.implicits._ // rightT and leftT syntax is here

  def getBandwidth(serverName: String): AsynResponse[Int] =
    bandwidths.get(serverName) match {
      case None    => EitherT.leftT("problem")
      case Some(b) => EitherT.rightT(b)
    }

  // TODO 1:
  def canWithstandSurge(s1: String, s2: String): AsynResponse[Boolean] =
    for {
      band1 <- getBandwidth(s1)
      band2 <- getBandwidth(s2)
    } yield band1 + band2 > 250

  // TODO 2:
  def generateTrafficSpikeReport(s1: String, s2: String): AsynResponse[String] =
    canWithstandSurge(s1, s2)
      .transform { // "transform" transforms an either into another either
        case Left(reason) =>
          Left(s"servers can't cope with with incoming spike: $reason")
        case Right(false) =>
          Left(
            s"servers can't cope with incoming the spike: not enough total bandwith"
          )
        case Right(true) =>
          Right(s"servers can cope with the incoming spike. no problem.")
      }

  def main(args: Array[String]): Unit = {
    println(listOfTuples.value)
    val res1 = generateTrafficSpikeReport("bla", "server2.rockthe.jvm").value
    val res2 = generateTrafficSpikeReport(
      "server1.rockthe.jvm",
      "server2.rockthe.jvm"
    ).value
    val res3 = generateTrafficSpikeReport(
      "server1.rockthe.jvm",
      "server3.rockthe.jvm"
    ).value

    res1.foreach(println)
    res2.foreach(println)
    res3.foreach(println)
  }

}
