package com.dixa.primes

import scala.concurrent.ExecutionContext
import scala.io.StdIn

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.Flow
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.common.EntityStreamingSupport


object PrimesProxy {

  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "PrimesProxy")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = PrimesServiceClient(GrpcClientSettings.fromConfig("dixa.PrimesService"))

    val matcher: PathMatcher1[Option[Int]] =
      "prime" / IntNumber.?

    // to stream results to CSV
    implicit val streamingSupport =
      EntityStreamingSupport.csv(maxLineLength = 16 * 1024)
        .withSupported(ContentTypeRange(ContentTypes.`text/plain(UTF-8)`))
        .withContentType(ContentTypes.`text/plain(UTF-8)`)
        .withFramingRenderer(Flow[ByteString].map(bs => bs ++ ByteString(",")))

    val route =
      path(matcher) {
        case Some(n) =>
          get {
            val responseStream =
              client.primesStream(PrimeRequest(n))
                .map(r => HttpEntity(ContentTypes.`text/plain(UTF-8)`, ByteString(r.prime.toString)))
            complete(responseStream)
          }
        case None =>
          complete(HttpResponse(StatusCodes.BadRequest, entity = "Missing <number> parameter"))
      }

    val bindingFuture = Http()
      .newServerAt("localhost", 8081)
      .bind(route)

    println(s"Proxy sever online, make a GET request to http://localhost:8081/prime/<number>\nPress RETURN to stop server")
    // let it run until user presses return
    StdIn.readLine()

    // trigger unbinding from the port and shutdown when done
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => sys.terminate())
  }

}