package com.catstudy.recap.part4

import cats.Monad

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}

object Traversing {
  implicit val ec: ExecutionContext = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(2))
  /*
  Problem:
  We have a:
  - data: List[String]
  - function: String => Future[Int]
  We want:
  - combination of elements from List[String] with function applied to it: but we want Future[List[Int]]
   */

  val servers: List[String] = List(
    "server-ci.rockthejvm.com",
    "server-staging.rockthejvm.com",
    "prod.rockthejvm.com"
  )
  def getBandwith(hostname: String): Future[Int] = Future(hostname.length * 60)

  val allBandwiths: Future[List[Int]] = servers.foldLeft(Future(List.empty[Int])){
    (accum, hostname) =>
    val band = getBandwith(hostname)
    for {
      accBand <- accum
      bandw <- band
    } yield accBand :+ bandw
  }

  // much simpler! traverse on future does exactly that!
  val allBandwithsTraverse = Future.traverse(servers)(getBandwith)
  // alternative:
  val allBandwithsSequence = Future.sequence(servers.map(getBandwith))

  // TODO 1:
  import cats.syntax.applicative._ // pure
  import cats.syntax.flatMap._ // flatmap
  import cats.syntax.functor._ // map
  def listTraverse[F[_] : Monad, A, B](list: List[A])(func: A => F[B]): F[List[B]] = {
    list.foldLeft(List.empty[B].pure[F]) {
      (accum, elem) =>
      val element = func(elem)
      for {
      acc <- accum
      e <- element
      } yield acc :+ e
    }
  }

  override def main(args: Array[String]): Unit = {

  }

}
