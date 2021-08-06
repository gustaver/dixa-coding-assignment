package com.dixa.primes

object Primes {
  def sieve(s: LazyList[Int]): LazyList[Int] =
    s.head #:: sieve(s.tail filter(_ % s.head != 0))

  val primes = sieve(LazyList.from(2))

  def primesUpTo(n: Int): LazyList[Int] = primes.takeWhile(_ <= n)
}
