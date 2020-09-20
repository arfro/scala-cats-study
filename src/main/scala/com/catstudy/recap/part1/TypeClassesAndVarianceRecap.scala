package com.catstudy.recap.part1

object TypeClassesAndVarianceRecap {

  // how variance affects variance in type classes?
  // Eq - type safe compare

  import cats.Eq // actual type class
  import cats.instances.int._ // type class instance: Eq[Int] comes in scope
  import cats.instances.option._ // type class instance: Eq[Option[_]] comes in scope with Int as _
  import cats.syntax.eq._ // extension methods

  val comparison = Option(2) === Option(5)

  // in the below with red highlight === because Some(3) is an instance of Some, not Option.
  // In other words: for the compiler Eq[Some[Int]] is missing. This is because of variance.
  // val invalidComparision = Some(3) === None

  // Variance recap: generic type annotation that let's propagating subclasses up to the generic type
  class Animal
  class Cat extends Animal

  // covariant type: subtypnig is propagated to the generic type
  class Cage[+T]

  val cage: Cage[Animal] = new Cage[Cat] // Ok! Because Cat <: Animal

  // contravariant type: subtyping is propagated backwards to the generic type
  // they are usually ACTION types. That is, something that can DO something to the animal

  class Vet[-T]

  val vet: Vet[Cat] = new Vet[Animal] // backwards! If Cat <: Animal than Vet[Animal] is a subtype of Vet[Cat]
  // I want on runtime to give you something better than just a vet of Cat, a generic Vet of ALL animals!

  // !!! RULE 1 (variance in general) !!!
  // "HAS a T" = covariant. If the precise type is not available, it can use the subtype.
  // "ACTS on T" = contravariant. If precise type is not available, it can use the super type.

  // variance affects how Type Class instances are fetched!

  trait SoundMaker[-T] // "maker" sounds like something that acts on T, so we're using contravariance here
  implicit object AnimalSoundMaker extends SoundMaker[Animal]
  // API
  def makeSound[T](implicit soundMaker: SoundMaker[T]): Unit = println("meow")
  makeSound[Animal] // OK. Compiler can find an implicit TC instance SoundMaker[Animal] in line 43.
  makeSound[Cat] // OK. TC instance for Animal is also applicable to cats.
  // Above the compiler is looking for SoundMaker[Cat] but finds SoundMaker[Animal]
  // and that's OK because SoundMaker takes a contravariant T. Which means we can use super type, here: Animal

  //... all of this relates to our broken line of code on top:
  // val invalidComparision = Some(3) === None
  // if we have Eq with contravariant T then
  implicit object OptionSoundMaker extends SoundMaker[Option[Int]]
  makeSound[Option[Int]] // OK!
  makeSound[Some[Int]] // OK!

  // !!! RULE 2 !!!
  // If your type class is contravariant you will be able to support all super types.
  // e.g. Option vs Some, Throw vs Try, Either vs Left

  // Eq from cats however is COVARIANT, so uses subtypes. Won't work for subtypes inside of it.
  // covariant Type Class
  trait AnimalShow[+T] {
    def show: String
  }

  implicit object GeneralAnimalShow extends AnimalShow[Animal] {
    override def show: String = "too many animals"
  }

  implicit object CatAnimalShow extends AnimalShow[Cat] {
    override def show: String = "too many cats"
  }

  def organizeShow[T](implicit event: AnimalShow[T]) = event.show

  def main(args: Array[String]): Unit = {
    // Covariant type classes will always use the most specific type available in scope.
    // If a type class instance for a general type is present (here: Animal) compiler will get confused. So:
    // In "organizeShow[Animal]" it sees Cat and Animal type class and gets confused
    println(organizeShow[Cat]) // OK.
    // println(organizeShow[Animal]) // OK in IDE but will throw compile error: ambiguous values.
  }
  // !!! RULE 3 !!!
  // You cannot have contra and co variance at the same time.
  // You have to pick whether you want to have all subtypes of the general type available OR all super types.

  // Cats: uses INVARIANCE in Type Classes !!!
  // ...so going back to the first line:
  // val invalidComparision = Some(3) === None
  // good solution would be to use smart constructors:
  Option(3) === Option.empty[Int] // OK <3

}
