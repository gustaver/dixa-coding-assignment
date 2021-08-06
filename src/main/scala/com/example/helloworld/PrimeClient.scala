package com.example.helloworld

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Framing
import akka.util.ByteString

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object PrimeClient {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val responseFuture: Future[HttpResponse] = Http().singleRequest(HttpRequest(uri = "http://localhost:8081/prime/10"))

    responseFuture
      .onComplete {
        case Success(res) => {
//          res.entity.toStrict(3.seconds)
//            .foreach(e => println(e.data.utf8String))
          res.entity.dataBytes
            .via(Framing.delimiter(ByteString(","), maximumFrameLength = 256))
            .map(b => b.utf8String)
            .runForeach(s => println(s"Got streaming prime $s"))
            .onComplete {
              case Success(_) => println("Done reading primes")
              case Failure(_) => println("Error when reading primes")
            }
        }
        case Failure(_)   => sys.error("something wrong")
      }
  }
}