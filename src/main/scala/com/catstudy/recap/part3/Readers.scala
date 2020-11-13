package com.catstudy.recap.part3

object Readers {

  // Purely functional way to implement DEPENDENCY INJECTION!

  // Readers are data structures that are a wrapper on how to derive B from A
  // Reader[Config, DBConfig] <- Reader() takes a function from Config to DBConfig.
  // They are very handy is you're in the middle of the application and read access to something from e.g. config or application startup.
  // They are a form of dependency injection..

  /**
    * PATTERN FOR READERS!
    * 1. Create the data structure that will be passed around (here: Configuration)
    * 2. Create readers that will describe how initial data structure will be used later (e.g. dbReader, httpReader)
    * 3. flatMap, map, fold, reduce pick an match the reader from step 2!!!
    * 4. Once you need the final result, call dbReader.run(config) passing the initial data structure (configuration)
    */
  case class Configuration(dbUser: String,
                           dbPass: String,
                           host: String,
                           port: Int,
                           nThreads: Int,
                           email: String)
  case class DbConnection(username: String, pass: String) {
    def getOrderStatus(orderId: Long): String = "dispatched"
    def getLastOrderId(username: String): Long = 12345432
  }
  case class HttpService(host: String, port: Int) {
    def start() = s"started server on port $port"
  }

  val config =
    Configuration("annette", "pass", "hostname", 12345, 4, "annnette@gmail.com")

  // cats Reader
  import cats.data.Reader
  // Reader takes a function from A => B as a constructor:
  val dbReader: Reader[Configuration, DbConnection] = Reader(
    config => DbConnection(config.dbUser, config.dbPass)
  )

  val dbConn = dbReader.run(config) // <- need to pass a config here

  // ^ but that seems like an overkill!

  // Reader[I, O] has a map though... so we can chain and get very specific value readers. That means that they compose!
  val annetteOrderStatusReader: Reader[Configuration, String] =
    dbReader.map(_.getOrderStatus(2345))
  val annetteOrderStatus = annetteOrderStatusReader.run(config)

  def getLastOrderStatus(username: String): String = {
    // composing readers here:
    val userLastOrderIdReader = dbReader
      .map(_.getLastOrderId(username))
      .flatMap(lastOrderId => dbReader.map(_.getOrderStatus(lastOrderId)))

    // could be much nicer as a for comprehension:
    for {
      lastOrderId <- dbReader.map(_.getLastOrderId(username))
      orderStatus <- dbReader.map(_.getOrderStatus(lastOrderId))
    } yield orderStatus

    userLastOrderIdReader.run(config)
  }

  // TODO: using readers implement "emailUser":
  case class EmailService(emailReplyTo: String) {
    def sendEmail(address: String, contents: String) =
      s"From $emailReplyTo with contents $contents"
  }

  def emailUser(username: String, userEmail: String) = {
    // fetch status of last order
    // email them with EmailService: "your last order has the status $orderstatus"
    val emailServiceReader: Reader[Configuration, EmailService] = Reader(
      config => EmailService(config.email)
    )
    val emailReader: Reader[Configuration, String] =
      for {
        lastOrderId <- dbReader.map(_.getLastOrderId(username))
        orderStatus <- dbReader.map(_.getOrderStatus(lastOrderId))
        emailService <- emailServiceReader
      } yield
        emailService.sendEmail(
          userEmail,
          s"your last order has status $orderStatus"
        )

    emailReader.run(config)
  }

  // TODO 2: what programming pattern does it remind you of? Dependency injection!

  def main(args: Array[String]): Unit = {
    println(getLastOrderStatus("daniel"))
    println(emailUser("annette", "annette@gmail.com"))
  }
}
