package com.example.ktorserver

import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import at.favre.lib.crypto.bcrypt.BCrypt

fun main() {
    Database.connect(
        url = "jdbc:postgresql://db.aunjxlhmqvaqpdqxzhpi.supabase.co:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "Edrfyf75432!"
    )

    transaction {
        SchemaUtils.create(Users)
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
                        call.respond(mapOf("token" to token))
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Неверный пароль"))
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Пользователь не найден"))
                }
            }

            authenticate("auth-jwt") {
                get("/secure") {
                    call.respond(mapOf("message" to "Access granted!"))
                }
            }
        }
    }.start(wait = true)
}

@Serializable
data class RegistrationRequest(
    val username: String,
    val password: String,
    val name: String,
    val lastName: String
)