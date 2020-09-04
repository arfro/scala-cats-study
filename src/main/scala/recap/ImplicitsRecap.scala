package recap

object ImplicitsRecap {
  case class Person(name: String) {
    def greet: String = s"Hi there, $name"
  }

  /* -------------------------------- */
  /* ----- 1. Implicit classes ------ */
  /* -------------------------------- */

  implicit class ImpersonableString(name: String) {
    def greet: String = Person(name).greet
  }

  // now we can call it explicitly:
  val impersonableString = new ImpersonableString("Peter")
  impersonableString.greet

  // or actually use the functionality and call it implicitly ("extend" String):
  "Peter".greet
  // compiler tries to find something that it can wrap a string with that happens to have a greet method.
  // above translates to: new ImpersonableString("Peter").greet
  // a.k.a extension method pattern

  /* ------------------------------- */
  /* ------ 2. Implicit vals ------- */
  /* ------------------------------- */

  trait JsonSerializer[T] {
    def toJson(value: T): String
  }

  def listToJson[T](list: List[T])(
    implicit serializer: JsonSerializer[T]
  ): String = { // note this can be a val OR a def
    list.map(value => serializer.toJson(value)).mkString("[", ",", "]")
  }

  def setToJson[T](set: Set[T])(
    implicit serializer: JsonSerializer[T]
  ): String = { // note this can be a val OR a def
    set.map(value => serializer.toJson(value)).mkString("[", ",", "]")
  }

  // like this it won't compile because we're missing the implicit value
  // setToJson(Set(Person("Bob"), Person("Greta")))

  implicit val personSerializer = new JsonSerializer[Person] {
    override def toJson(value: Person): String =
      s"""
        |{"person": "$value"
        |""".stripMargin
  }

  //now it will compile
  setToJson(Set(Person("Bob"), Person("Greta")))

  /* ---------------------------------- */
  /* ------ 3. Implicit methods ------- */
  /* ---------------------------------- */

  // The downside of the above is that we have to define that implicit val for all types other than Person.
  // Something more generic would be an implicit def.
  // T <: Product means: all case classes

  implicit def oneArgCaseClassSerializer[T <: Product]: JsonSerializer[T] =
    new JsonSerializer[T] {
      override def toJson(value: T): String =
        s"""
        |"${value.productElementName(0)}": "${value.productElement(0)}" 
        |""".stripMargin.trim
    }
  // productElementName: that one value from a case class, for person: Name.
  // productElement: the value of that field

  case class Cat(name: String)

  println(listToJson(List(Cat("abc"), Cat("test"))))
  // above prints ["name": "abc","name": "def"]
  // in the background compiler will call: listToJson(List(Cat("abc"), Cat("def")))(oneArgCaseClassSerializer[Cat])

  /* ------------------------------------------------------------- */
  /* ------ 4. Implicit methods - conversion (discouraged!)------- */
  /* ------------------------------------------------------------- */

  // use implicit class instead to do the extension on a type, e.g. "String".convert
  // otherwise users might be converting without even knowing types are being converted

}
