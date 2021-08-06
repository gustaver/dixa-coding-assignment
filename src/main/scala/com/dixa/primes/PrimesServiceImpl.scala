package com.dixa.primes

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source

class PrimesServiceImpl(system: ActorSystem[_]) extends PrimesService {
  private implicit val sys: ActorSystem[_] = system

  override def primesStream(in: PrimeRequest): Source[PrimeResponse, NotUsed] = {
    val n = in.n
    Source(Primes.efficientPrimesUpTo(n)).map(p => PrimeResponse(p))
  }
}
