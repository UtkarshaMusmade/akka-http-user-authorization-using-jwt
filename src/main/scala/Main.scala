package com.knoldus

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.knoldus.routes.UserManagementRoutes
import com.knoldus.services.UserManagementService
import com.typesafe.config.ConfigFactory

import scala.concurrent.ExecutionContextExecutor
import scala.io.StdIn

object Main extends App {

  val conf = ConfigFactory.load()

  implicit val actorSystem: ActorSystem = ActorSystem("system")
  implicit val materializer: ActorMaterializer =
    ActorMaterializer()(actorSystem)
  implicit val executionContext: ExecutionContextExecutor = actorSystem.dispatcher
  val userManagementService = new UserManagementService
  val userMananagementRoutes = new UserManagementRoutes(userManagementService)
  val routes = userMananagementRoutes.routes

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8081)
  println(s"Server online at http://localhost:8081/\nPress RETURN to stop...")
  StdIn.readLine()
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => actorSystem.terminate())

}
