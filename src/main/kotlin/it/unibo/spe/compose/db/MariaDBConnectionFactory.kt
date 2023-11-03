package it.unibo.spe.compose.db

import java.sql.Connection

class MariaDBConnectionFactory(
    override val database: String,
    override val username: String,
    override val password: String,
    override val host: String,
    override val port: Int,
) : MariaDB, ConnectionFactory {
    override fun connect(): Connection =
        MariaDBConnection(database, username, password, host, port)
}
