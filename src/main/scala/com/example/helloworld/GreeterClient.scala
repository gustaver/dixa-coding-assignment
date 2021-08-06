package com.example.helloworld

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure
import scala.util.Success
import scala.io.StdIn
import akka.Done
import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.grpc.GrpcClientSettings
import akka.stream.scaladsl.{Flow, Source}
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.common.EntityStreamingSupport
import akka.http.scaladsl.marshalling.{Marshaller, Marshalling}


object GreeterClient {

  def main(args: Array[String]): Unit = {
    implicit val sys: ActorSystem[_] = ActorSystem(Behaviors.empty, "GreeterClient")
    implicit val ec: ExecutionContext = sys.executionContext

    val client = GreeterServiceClient(GrpcClientSettings.fromConfig("helloworld.GreeterService"))

    def singleRequestReply(n: Int): Unit = {
      println(s"Performing request: $n")
      val responseStream = client.itKeepsReplying(HelloRequest(n))
      val done: Future[Done] =
        responseStream.runForeach(reply => println(s"$n got streaming reply: ${reply.prime}"))
      done.onComplete {
        case Success(_) =>
          println("streamingBroadcast done")
        case Failure(e) =>
          println(s"Error streamingBroadcast: $e")
      }
    }

    val matcher: PathMatcher1[Option[Int]] =
      "prime" / IntNumber.?

    implicit val streamingSupport =
      EntityStreamingSupport.csv(maxLineLength = 16 * 1024)
        .withSupported(ContentTypeRange(ContentTypes.`text/plain(UTF-8)`))
        .withContentType(ContentTypes.`text/plain(UTF-8)`)
        .withFramingRenderer(Flow[ByteString].map(bs => bs ++ ByteString(",")))

    val route =
      path(matcher) { n: Option[Int] =>
        get {
          val responseStream =
            client.itKeepsReplying(HelloRequest(n.get))
              .map(r => HttpEntity(ContentTypes.`text/plain(UTF-8)`, ByteString(r.prime.toString)))
          complete(responseStream)
        }
      }

    val bindingFuture = Http().newServerAt("localhost", 8081).bind(route)

    println(s"Server now online. Please navigate to http://localhost:8080/hello\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => sys.terminate()) // and shutdown when done
  }

}