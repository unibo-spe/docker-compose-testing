package it.unibo.spe.compose.db

interface MariaDB {
    val database: String
    val username: String
    val password: String
    val host: String
    val port: Int

    companion object {
        const val DEFAULT_HOST = "localhost"
        const val DEFAULT_PORT = 3306
    }
}
