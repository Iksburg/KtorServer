package com.example.ktorserver

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object Sleeps : Table("dreams") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val title = varchar("title", 255)
    val description = text("description")
    val date = timestamp("date")

    override val primaryKey = PrimaryKey(id)
}