package com.example.helloworld

import scala.concurrent.Future

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.BroadcastHub
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.MergeHub
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source

class GreeterServiceImpl(system: ActorSystem[_]) extends GreeterService {
  private implicit val sys: ActorSystem[_] = system

  override def itKeepsReplying(in: HelloRequest): Source[HelloReply, NotUsed] = {
    println(s"itKeepsReplying n ${in.n}")
    val primes = List(1, 2, 3, 5, 7)
    Source(primes).map(p => HelloReply(p))
  }
}
