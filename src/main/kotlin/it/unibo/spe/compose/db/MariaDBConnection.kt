package it.unibo.spe.compose.db

import java.sql.Connection
import java.sql.DriverManager

class MariaDBConnection(
    override val database: String,
    override val username: String,
    override val password: String,
    override val host: String = MariaDB.DEFAULT_HOST,
    override val port: Int = MariaDB.DEFAULT_PORT,
) : MariaDB,
    Connection by DriverManager.getConnection("jdbc:mariadb://$host:$port/$database", username, password)
