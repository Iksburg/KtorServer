package com.example.ktorserver

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 255)
    val password = varchar("password", 255)
    val name = varchar("name", 255)
    val lastName = varchar("lastName", 255)

    override val primaryKey = PrimaryKey(id)
}