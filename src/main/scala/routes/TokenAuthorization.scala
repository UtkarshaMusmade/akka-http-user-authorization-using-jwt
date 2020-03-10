package com.knoldus.routes

import java.util.concurrent.TimeUnit

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, provide}
import authentikat.jwt.{JsonWebToken, JwtClaimsSet, JwtHeader}

object TokenAuthorization {
  private val secretKey = "super_secret_key"
  private val header = JwtHeader("HS256")
  private val tokenExpiryPeriodInDays = 1

  def generateToken(email: String): String = {
    println("in generate token")

    val claims = JwtClaimsSet(
      Map(
        "email" -> email,
        "expiredAt" -> (System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(tokenExpiryPeriodInDays))
      )
    )
    JsonWebToken(header, claims, secretKey)
  }

  def authenticated: Directive1[Map[String, Any]] = {

    optionalHeaderValueByName("Authorization").flatMap { tokenFromUser =>

      val jwtToken = tokenFromUser.get.split(" ")
      jwtToken(1) match {
        case token if isTokenExpired(token) =>
          complete(StatusCodes.Unauthorized -> "Session expired.")

        case token if JsonWebToken.validate(token, secretKey) =>
          provide(getClaims(token))

        case _ =>  complete(StatusCodes.Unauthorized ->"Invalid Token")
      }
    }
  }

  private def isTokenExpired(jwt: String): Boolean =
    getClaims(jwt).get("expiredAt").exists(_.toLong < System.currentTimeMillis())

  private def getClaims(jwt: String): Map[String, String] =
    JsonWebToken.unapply(jwt) match {
      case Some(value) => value._2.asSimpleMap.getOrElse(Map.empty[String, String])
      case None => Map.empty[String, String]

    }
}