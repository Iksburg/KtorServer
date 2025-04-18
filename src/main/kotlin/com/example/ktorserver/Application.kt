package com.example.ktorserver

import at.favre.lib.crypto.bcrypt.BCrypt
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant


fun main() {
    Database.connect(
        url = "jdbc:postgresql://db.aunjxlhmqvaqpdqxzhpi.supabase.co:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "Edrfyf75432!"
    )

    transaction {
        SchemaUtils.create(Users, Sleeps)
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(ContentNegotiation) {
            json()
        }

        install(Authentication) {
            jwt("auth-jwt") {
                realm = "ktor sample app"
                verifier(JwtConfig.verifier)
                validate {
                    val username = it.payload.getClaim("username").asString()
                    if (username != null) JWTPrincipal(it.payload) else null
                }
            }
        }

        routing {
            post("/register") {
                try {
                    val request = call.receive<RegistrationRequest>()

                    val hashedPassword = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

                    transaction {
                        Users.insert {
                            it[username] = request.username
                            it[password] = hashedPassword
                            it[name] = request.name
                            it[lastName] = request.lastName
                        }
                    }

                    call.respond(mapOf("status" to "registered"))
                } catch (e: Exception) {
                    e.printStackTrace()
                    call.respondText("Ошибка при регистрации: ${e.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            post("/login") {
                val post = call.receive<Map<String, String>>()
                val usernameInput = post["username"]
                val passwordInput = post["password"]

                val userRecord = transaction {
                    Users.select {
                        Users.username eq usernameInput!!
                    }.singleOrNull()
                }

                if (userRecord != null) {
                    val storedHash = userRecord[Users.password]
                    val result = BCrypt.verifyer().verify(passwordInput!!.toCharArray(), storedHash)

                    if (result.verified) {
                        val token = JwtConfig.makeToken(usernameInput.toString())
                        val userId = userRecord[Users.id]
                        call.respond(LoginResponse(token, userId))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Неверный пароль"))
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Пользователь не найден"))
                }
            }

            post("/sleep") {
                val sleepRequest = call.receive<SleepRequest>()

                val instant = Instant.parse(sleepRequest.date)

                transaction {
                    Sleeps.insert {
                        it[userId] = sleepRequest.userId
                        it[title] = sleepRequest.title
                        it[description] = sleepRequest.description
                        it[date] = instant
                    }
                }

                call.respond(HttpStatusCode.OK, mapOf("status" to "saved"))
            }

            get("/sleeps/{userId}") {
                val userIdParam = call.parameters["userId"]?.toIntOrNull()

                if (userIdParam == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Неверный userId"))
                    return@get
                }

                val sleeps = transaction {
                    Sleeps.select {
                        Sleeps.userId eq userIdParam
                    }.map {
                        SleepResponse(
                            title = it[Sleeps.title],
                            description = it[Sleeps.description],
                            date = it[Sleeps.date].toString()
                        )
                    }
                }

                call.respond(sleeps)
            }

            authenticate("auth-jwt") {
                get("/secure") {
                    call.respond(mapOf("message" to "Access granted!"))
                }
            }
        }
    }.start(wait = true)
}