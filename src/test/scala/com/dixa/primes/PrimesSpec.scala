package com.dixa.primes

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Sink
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import scala.concurrent.duration._

class PrimesSpec
  extends AnyWordSpec
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures {

  implicit val patience: PatienceConfig = PatienceConfig(scaled(5.seconds), scaled(100.millis))

  // important to enable HTTP/2 in server ActorSystem's config
  val conf = ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
    .withFallback(ConfigFactory.defaultApplication())

  val testKit = ActorTestKit(conf)

  val serverSystem: ActorSystem[_] = testKit.system
  val bound = new PrimesServer(serverSystem).run()

  // make sure server is bound before using client
  bound.futureValue

  implicit val clientSystem: ActorSystem[_] = ActorSystem(Behaviors.empty, "PrimesClient")

  val client =
    PrimesServiceClient(GrpcClientSettings.fromConfig("dixa.PrimesService"))

  override def afterAll: Unit = {
    ActorTestKit.shutdown(clientSystem)
    testKit.shutdownTestKit()
  }

  "GreeterService" should {
    "primes up to 2" in {
      val reply = client.primesStream(PrimeRequest(2))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2))
    }

    "primes up to 10" in {
      val reply = client.primesStream(PrimeRequest(10))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2, 3, 5, 7))
    }

    "primes up to 100" in {
      val reply = client.primesStream(PrimeRequest(100))
        .map(r => r.prime)
        .runWith(Sink.seq)

      reply.futureValue should be(Vector(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61,
        67, 71, 73, 79, 83, 89, 97))
    }
  }
}