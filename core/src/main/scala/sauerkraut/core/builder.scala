package sauerkraut
package core

import format.FastTypeTag

/** 
 * Represents something that we can construct a builder for. 
 */
trait Buildable[T]
  /** Construct a new builder that can generate type T. */
  def newBuilder: Builder[T]

/** 
 * Represents something that can build type T out of a pickle.
 * 
 * This is a push API, where read values are pushed into the builder, constructing the type.
 */
sealed trait Builder[T]
  def result: T

/** Represents a `builder` that can be used to generate a structure from a pickle. */
trait StructureBuilder[T] extends Builder[T]
  /** The known field names for this structure. */
  def knownFieldNames: List[String]
  /** 
   * Puts a field into this builder.
   * 
   * Note: For a field that are collections, this may be called mulitple times
   * with individual elements.
   */
  def putField[F](name: String): Builder[F]
  /** Returns the resulting built structure after pushing in all pieces of data. */
  def result: T


trait CollectionBuilder[E, To] extends Builder[To]
  /** Places an element into the collection.   Should be read from the Pickle. */
  def putElement(): Builder[E]
  /** Returns the built collection of elements. */
  def result: To

trait PrimitiveBuilder[P] extends Builder[P]
  def putPrimitive(value: P): Unit


// Now we attempt to derive a builder.
object Buildable
  import deriving._
  import scala.compiletime.{erasedValue,constValue,summonFrom}
  import internal.InlineHelper.summonLabels
  inline def derived[T](given m: Mirror.Of[T]): Buildable[T] =
    new Buildable[T] {
        override def newBuilder: Builder[T] =
          inline m match
            case m: Mirror.ProductOf[T] =>
               productBuilder[T, m.MirroredElemTypes, m.MirroredElemLabels]
            case _ => compiletime.error("Cannot derive builder for non-struct classes")
    }
  inline def productBuilder[T, Fields <: Tuple, Labels <: Tuple]: Builder[T] = 
    new StructureBuilder[T] {
        override def knownFieldNames: List[String] =
          summonLabels[Labels]
        override def putField[F](name: String): Builder[F] = ???
        override def result: T = ???
    }