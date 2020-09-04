package com.catstudy.recap

object TypeClassesRecap {

  // ------ Type Class Pattern ------
  // consists of three parts:
  // 1. type class definition - abstract class or a trait

  trait JsonSerializer[T] {
    def toJson(value: T): String // this is the capability we want to add ad hoc to classes that will be T
  }
  // 2. Implicit type class instances. Because the above is a generic we need concrete implementations
  implicit object StringSerializer extends JsonSerializer[String] { // for String
    override def toJson(value: String): String =
      s"""
         |$value
         |""".stripMargin.trim
  }

  implicit object PersonSerializer extends JsonSerializer[Person] { // for Person
    override def toJson(value: Person): String =
      s"""
         |{"name": "${value.name}", "age": ${value.age}}
         |""".stripMargin.trim
  }

  // part 3. offer API to access the above. Now we will create
  def convertListToJson[T](
    list: List[T]
  )(implicit jsonSerializer: JsonSerializer[T]) = {
    list.map(v => jsonSerializer.toJson(v)).mkString("[", ",", "]")
  }

  // part 4. (optional!) EXTENSION METHODS
  object JsonOps {
    implicit class JsonSerializable[T](value: T)(
      implicit jsonSerializer: JsonSerializer[T]
    ) {
      def toJson: String = jsonSerializer.toJson(value)
    }
  }

  // just some case classes that we can use with the code above
  case class Person(name: String, age: Int)

  def main(args: Array[String]): Unit = {
    // this can be run on any List of type class instances that we already have.
    // Right now we can only run it on String and Person (those two we have defined implicit object instances for)
    println(convertListToJson(List(Person("abc", 3), Person("def", 7))))
    import JsonOps._ // this is where our Extension method is
    println(Person("ggg", 4).toJson)
  }
}
