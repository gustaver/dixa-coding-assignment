package com.dixa.primes

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.scaladsl.Framing
import akka.util.ByteString

import scala.util.{Failure, Success}

object PrimesClient {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "SingleRequest")
    // needed for the future flatMap/onComplete in the end
    implicit val executionContext = system.executionContext

    val n = args.head.toInt

    Http()
      .singleRequest(HttpRequest(uri = s"http://localhost:8081/prime/$n"))
      .onComplete {
        case Success(res) => {
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