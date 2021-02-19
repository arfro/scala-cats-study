package com.catstudy.recap.part3

import cats.kernel.Semigroup
import com.catstudy.recap.part3.DataValidation.FormValidation.validateForm

import scala.util.Try

object DataValidation {

  /**
   * Very very useful!
   * New data structure to encapsulate data validation logics + errors in case anything fails.
   * It's cats.data.Validated. It's like Either.
   */

  import cats.data.Validated

  val aValidValue: Validated[String, Int] = Validated.valid(19) // aka. Right in Either
  val anInvalidValue: Validated[String, Int] = Validated.invalid("oops") // aka. Left in Either
  // it has APIs to test a condition and return an invalid/valid depending on the data.
  val aTest: Validated[String, Int] = Validated.cond(42 > 44, 99, "invalid")

  // WHY not to use Either if its the same?
  // It has a different contract: it will combine all the errors into one value using no mutations, just pure FP
  //TODO: use Either: n must be even, n must be non-negative, n <= 100, n must be even.
  // ^ if it fails then appropriate strings need to be held in the list containing strings.
  def testNumber(n: Int): Either[List[String], Int] = {
    val isNegative = if(n < 0) List("negative") else List()
    val isGreater100 = if(n > 100) List("> 100") else List()
    val isOdd = if(n / 2 != 1) List("odd") else List()

    val full = isNegative ++ isGreater100 ++ isOdd
    if(full == 0) Right(n)
    else Left(full)
  }
  // ^ this looks super ugly. So we will use cats Validated to combine all the errors
  import cats.instances.list._ // semigroup for list
  //import cats.instances.int._ // semigroup for int, but! combining will COMBINE, so ADD integers. So we need something that for 'combine' will return the samae number, here: n
  implicit val semigroupInt = Semigroup.instance[Int]((one, two) => Math.max(one, two)) // now we will be able have a custom definition of 'combine'. Here it means: return greater.
  def validateNumber(n: Int): Validated[List[String], Int] = {
    Validated.cond(n % 2 == 0, n, List("number must be even"))
      .combine(Validated.cond(n < 100, n, List("must be less than 100")))
  }

  // Validated can be chained with functions like flatMap, its called "andThen"
  // its not called "flatmap" because flatmap short circuits. andThen does not!
  aValidValue.andThen(v => anInvalidValue).andThen(v => aValidValue)
  // test the value, "double check" the condition STILL stands true
  aValidValue.ensure(List("oops"))(_ % 2 == 0)
  // transform Validated instances
  aValidValue.map(_ + 5)
  aValidValue.leftMap(_.length) // map on the error type
  aValidValue.bimap(_.length, _ + 1) // map on the left and on the right
  // interrogate with stdLib
  val eitherToValidated: Validated[List[String], Int] = Validated.fromEither(Right(3))
  val optionToValidated: Validated[List[String], Int] = Validated.fromOption(Some(4), List("oops"))
  val tryToValidated: Validated[Throwable, Int] = Validated.fromTry(Try(2))

  // what about from validated to option? or to either?
  aValidValue.toOption
  aValidValue.toEither

  // TODO: form validation excercise
  object FormValidation {
    type FormValidation[T] = Validated[List[String], T]

    /*
    - fields are: name, email, password
    - rule: must all be present
    - rule: name must not be blank
    - rule: email must be a valid email (have @)
    - rule: password must be at least 20 characters long
     */
    def getValue(form: Map[String, String], key: String): FormValidation[String] =
      Validated.fromOption(form.get(key), List(s"$key missing"))

    def nonBlank(value: String, key: String): FormValidation[String] = {
      Validated.cond(value.length > 0, value, List(s"$key must not be blank"))
    }

    def validEmail(email: String): FormValidation[String] = {
      Validated.cond(email.contains("@"), email, List(s"not a valid email"))
    }

    def validPassword(password: String): FormValidation[String] = {
      Validated.cond(password.length > 20, password, List(s"password not long enough"))
    }


    def validateForm(form: Map[String, String]): FormValidation[String] = {
      import cats.instances.string._ // this will concatenate all strings but we dont actually care
      getValue(form, "name").andThen(nonBlank(_, "name"))
        .combine(getValue(form, "password").andThen(validPassword(_)))
        .combine(getValue(form, "email").andThen(validEmail(_)))
        .map(_ => "Success")
    }
  }

  // We can also use the nice syntax:
  import cats.syntax.validated._
  val aValidMeaningOfLife: Validated[List[String], Int] = 42.valid[List[String]]
  val anError: Validated[String, Int] = "oops".invalid[Int]

  def main(args: Array[String]): Unit = {

    println(validateNumber(101))
    // soooo cool!
    println(validateForm(Map("name" -> "annette", "password" -> "234234", "email" -> "hello")))
    println(validateForm(Map("name" -> "annette", "password" -> "234234", "email" -> "hello@me.com")))
    println(validateForm(Map("name" -> "annette", "password" -> "qwertyuioiuytrewqwertyuio", "email" -> "hello@me.com")))
    println(validateForm(Map("password" -> "qwertyuioiuytrewqwertyuio", "email" -> "hello@me.com")))

  }
}
