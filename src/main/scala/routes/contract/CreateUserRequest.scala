package com.knoldus.routes.contract

import play.api.libs.json.{Json, Reads}

case class CreateUserRequest(
                              email: String,
                              password: String,
                              confirmPassword: String
                            )

object CreateUserRequest {
  implicit val createUserRequestReads: Reads[CreateUserRequest] = Json.reads[CreateUserRequest]
}