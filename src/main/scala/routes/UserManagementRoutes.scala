package com.knoldus.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.knoldus.routes.contract.{CreateUserRequest, LoginRequest}
import com.knoldus.services.UserManagementService
import de.heikoseeberger.akkahttpplayjson.PlayJsonSupport

class UserManagementRoutes(service: UserManagementService) extends PlayJsonSupport {
  val routes: Route =
    pathPrefix("user") {
      path("login") {
        (post & entity(as[LoginRequest])) { loginRequest =>
          if (service.userLogin(loginRequest) == "Login Successful") {
            val token = TokenAuthorization.generateToken(loginRequest.email)
            complete((StatusCodes.OK, token))
          } else {
            complete(StatusCodes.Unauthorized)
          }
        }
      } ~
        path("register") {
          (post & entity(as[CreateUserRequest])) { createUserRequest =>
            if (service.createUser(createUserRequest) == "User Created") {
              val token: String = TokenAuthorization.generateToken(createUserRequest.email)
              complete((StatusCodes.OK, token))
            } else {
              complete(StatusCodes.BadRequest -> "Cannot Register")
            }
          }
        }
    } ~
      path("protectedcontent") {
        get {
          TokenAuthorization.authenticated { _ =>
            val response = service.protectedContent
            println(response)
            complete(response)
          }
        }
      }
}

