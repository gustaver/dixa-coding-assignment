package com.dixa.primes

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl._

import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class PrimesServiceImplSpec
  extends AnyWordSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  val testKit = ActorTestKit()

  implicit val patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  implicit val system: ActorSystem[_] = testKit.system

  val service = new PrimesServiceImpl(system)

  override def afterAll(): Unit = {
    testKit.shutdownTestKit()
  }

  "PrimesServiceImpl" should {
    "primes up to 2" in {
      val reply = service.primesStream(PrimeRequest(2))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2))
    }

    "primes up to 10" in {
      val reply = service.primesStream(PrimeRequest(10))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2, 3, 5, 7))
    }

    "primes up to 100" in {
      val reply = service.primesStream(PrimeRequest(100))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61,
        67, 71, 73, 79, 83, 89, 97))
    }
  }
}
