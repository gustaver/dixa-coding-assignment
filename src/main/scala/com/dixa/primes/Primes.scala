package com.dixa.primes

object Primes {
  def sieve(s: LazyList[Int]): LazyList[Int] =
    s.head #:: sieve(s.tail filter(_ % s.head != 0))

  val primes = sieve(LazyList.from(2))

  def primesUpTo(n: Int): LazyList[Int] = primes.takeWhile(_ <= n)

  def efficientPrimesUpTo(n: Int): LazyList[Int] = {
    val odds = LazyList.from(3, 2).takeWhile(_ <= Math.sqrt(n).toInt)
    val composites = odds.flatMap(i => LazyList.from(i * i, 2 * i).takeWhile(_ <= n))
    2 #:: LazyList.from(3, 2).takeWhile(_ <= n).diff(composites)
  }
}
