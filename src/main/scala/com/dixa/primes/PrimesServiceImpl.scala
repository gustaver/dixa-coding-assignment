package com.dixa.primes

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.BroadcastHub
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.MergeHub
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

class PrimesServiceImpl(system: ActorSystem[_]) extends PrimesService {
  private implicit val sys: ActorSystem[_] = system

  override def primesStream(in: PrimeRequest): Source[PrimeResponse, NotUsed] = {
    val n = in.n
    println(s"primesStream requested for n: $n")
    Source(Primes.primesUpTo(n)).map(p => PrimeResponse(p))
  }
}
