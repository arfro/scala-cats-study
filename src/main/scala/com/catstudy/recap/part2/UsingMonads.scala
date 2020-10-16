package com.catstudy.recap.part2

object UsingMonads {

  // recap
  import cats.Monad
  import cats.instances.list._

  val monadList = Monad[List] // fetch implicit monad[list]
  val simpleList = monadList.pure(List(1, 2, 3, 4))

  // either
  val manualEither: Either[String, Int] = Right(13)
  type LoadingOr[T] = Either[String, T] // T is the desirable value. Loading is a string e.g. loading message
  type ErrorOr[T] = Either[Throwable, T] // String is an error

  import cats.instances.either._
  val loadingMonad = Monad[LoadingOr]
  val ok = loadingMonad.pure(Right(33))
  val loading = loadingMonad.pure(Left("loading"))
  // i can also chain and transform from Either to Either using flatMap

  // imaginary online store. Tracking status of orders
  case class OrderStatus(orderId: Long, status: String)

  def getOrderStatus(orderId: Long): LoadingOr[OrderStatus] =
    Right(OrderStatus(orderId, "ready to ship"))

  def trackLocation(orderStatus: OrderStatus): LoadingOr[String] =
    if (orderStatus.orderId > 1000) Left("not available yet")
    else Right("amsterdam")

  val orderNr = 12345L
  // combining using flatMaps
  val orderLocation = loadingMonad.flatMap(getOrderStatus(orderNr))(
    orderStatus => trackLocation(orderStatus)
  )

  // to use for comprehension we need to have flatMap and map sytax in scope:
  import cats.syntax.flatMap._
  import cats.syntax.functor._

  for {
    orderStatus <- getOrderStatus(orderNr)
    result <- trackLocation(orderStatus)
  } yield result

  // Exercises: the service layer API of a web app
  case class Connection(host: String, port: String)
  val cofig = Map("host" -> "localhost", "port" -> "3030")

  trait HTTPService[M[_]] {
    def getConnection(con: Map[String, String]): M[Connection]
    def issueRequest(con: Connection, payload: String): M[String]
  }
  // DO NOT CHANGE CODE.
  // Exercise 1: real impl HttpService using any kind of type for M: Optiom, Try, Either..
  object OptionHttpService extends HTTPService[Option] {
    override def getConnection(con: Map[String, String]): Option[Connection] =
      for {
        host <- con.get("host")
        port <- con.get("port")
      } yield Connection(host, port)

    override def issueRequest(con: Connection,
                              payload: String): Option[String] =
      if (payload.length > 20) None
      else Some(s"payload '$payload' accepted")
  }

  // Exercise 2: implement HttpService using ErrorOr or LoadingOr
  object AggressiveHttpService extends HTTPService[ErrorOr] {
    override def getConnection(
      con: Map[String, String]
    ): ErrorOr[Connection] = {
      val maybeCOn = for {
        host <- con.get("host")
        port <- con.get("port")
      } yield Connection(host, port)

      maybeCOn match {
        case Some(con) => Right(con)
        case None      => Left(throw new Exception("oops"))
      }

    }

    override def issueRequest(con: Connection,
                              payload: String): ErrorOr[String] = {
      if (payload.length > 20) Left(throw new Exception("error"))
      else Right("accepted")
    }
  }

  // !!!! improvement !!!! all of the above can be generalised though...
  def getResponse[M[_]: Monad](service: HTTPService[M],
                               payload: String): M[String] =
    for {
      conn <- service.getConnection(cofig)
      resp <- service.issueRequest(conn, payload)
    } yield resp

  def main(args: Array[String]): Unit = {
    // exercise 1 test
    val responsOption = OptionHttpService
      .getConnection(cofig)
      .flatMap(con => OptionHttpService.issueRequest(con, "hello there"))

    println(responsOption)

    // exervise 2 test
    val errorOrResponse = AggressiveHttpService
      .getConnection(cofig)
      .flatMap(AggressiveHttpService.issueRequest(_, "hello"))

    println(errorOrResponse)

    // !!! improvement !!!  test
    println(getResponse(AggressiveHttpService, "hello"))
    import cats.instances.option._
    println(getResponse(OptionHttpService, "hello"))
  }
}
