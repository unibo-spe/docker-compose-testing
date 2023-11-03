package it.unibo.spe.compose.db

import java.sql.Connection

fun interface ConnectionFactory {
    fun connect(): Connection
}
