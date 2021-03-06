package spire.math

import spire.algebra._

import scala.{specialized => spec}
import scala.annotation.tailrec
import scala.math.{ScalaNumber, ScalaNumericConversions}

import spire.implicits._

object Gaussian {
  def i[@spec(Int, Long) T](implicit f: Integral[T]) =
    new Gaussian(f.zero, f.one)

  def one[@spec(Int, Long) T](implicit f: Integral[T]) =
    new Gaussian(f.one, f.zero)

  def zero[@spec(Int, Long) T](implicit f: Integral[T]) =
    new Gaussian(f.zero, f.zero)

  def fromInt[@spec(Int, Long) T](n: Int)(implicit f: Integral[T]) =
    new Gaussian(f.fromInt(n), f.zero)

  implicit def intToGaussian(n:Int) = new Gaussian[Int](n, 0)
  implicit def longToGaussian(n:Long) = new Gaussian[Long](n, 0L)
  implicit def bigIntToGaussian(n:BigInt) = new Gaussian[BigInt](n, BigInt(0))

  def apply[@spec(Int, Long) T](real: T)(implicit f: Integral[T]): Gaussian[T] =
    new Gaussian(real, f.zero)
}

final case class Gaussian[@spec(Int, Long) T](real: T, imag: T)(implicit f: Integral[T])
    extends ScalaNumber with ScalaNumericConversions with Serializable {

  override def toString(): String = "(%s+%si)" format (real, imag)

  def doubleValue: Double = f.toDouble(real)
  def floatValue: Float = f.toFloat(real)
  def longValue: Long = f.toLong(real)
  def intValue: Int = f.toInt(real)
  override def shortValue: Short = f.toShort(real)
  override def byteValue: Byte = f.toByte(real)

  def isWhole: Boolean = true
  def signum: Int = f.compare(real, f.zero)
  def underlying: (T, T) = (real, imag)

  override def hashCode: Int = 19 * real.## + 41 * imag.## + 97

  // not typesafe, so this is the best we can do :(
  override def equals(that: Any): Boolean = that match {
    case that: Gaussian[_] => real == that.real && imag == that.imag
    case that => unifiedPrimitiveEquals(that)
  }

  def norm: T = f.plus(f.times(real, real), f.times(imag, imag))
  def conjugate: Gaussian[T] = new Gaussian(real, f.negate(imag))

  def asTuple: (T, T) = (real, imag)

  def isZero: Boolean = f.eqv(real, f.zero) && f.eqv(imag, f.zero)
  def isImaginary: Boolean = f.eqv(real, f.zero)
  def isReal: Boolean = f.eqv(imag, f.zero)

  def eqv(b: Gaussian[T]): Boolean = 
    f.eqv(real, b.real) && f.eqv(imag, b.imag)

  def neqv(b: Gaussian[T]): Boolean =
    f.neqv(real, b.real) || f.neqv(imag, b.imag)

  def unary_-(): Gaussian[T] =
    new Gaussian(f.negate(real), f.negate(imag))

  def +(b: Gaussian[T]): Gaussian[T] =
    new Gaussian(f.plus(real, b.real), f.plus(imag, b.imag))

  def -(b: Gaussian[T]): Gaussian[T] =
    new Gaussian(f.minus(real, b.real), f.minus(imag, b.imag))

  def *(b: Gaussian[T]): Gaussian[T] = new Gaussian(
    f.minus(f.times(real, b.real), f.times(imag, b.imag)),
    f.plus(f.times(imag, b.real), f.times(real, b.imag))
  )

  def /(b: Gaussian[T]): Gaussian[T] = {
    val n = b.norm
    val r = f.quot(f.plus(f.times(real, b.real), f.times(imag, b.imag)), n)
    val j = f.quot(f.minus(f.times(imag, b.real), f.times(real, b.imag)), n)
    new Gaussian(r, j)
  }

  def /~(b: Gaussian[T]): Gaussian[T] = this / b

  def %(b: Gaussian[T]): Gaussian[T] = this - (this / b) * b

  def /%(b: Gaussian[T]): (Gaussian[T], Gaussian[T]) = {
    val q = this / b
    (q, this - q * b)
  }

  def **(n:Int): Gaussian[T] = pow(n)

  def pow(n:Int): Gaussian[T] = {
    if (n < 0) sys.error("illegal exponent: %s" format n)

    @tailrec
    def recur(g: Gaussian[T], n: Int, sofar: Gaussian[T]): Gaussian[T] =
      if (n == 0) sofar
      else if ((n & 1) == 1) recur(g * g, n >> 1, g * sofar)
      else recur(g * g, n >> 1, sofar)

    recur(this, n, Gaussian.one[T])
  }
}
