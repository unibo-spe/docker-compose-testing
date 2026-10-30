package it.unibo.spe.compose

import it.unibo.spe.compose.db.MariaDB
import it.unibo.spe.compose.db.MariaDBConnectionFactory
import it.unibo.spe.compose.impl.SqlCustomerRepository
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import java.io.File
import java.lang.IllegalArgumentException
import java.net.ServerSocket
import java.nio.file.Files
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestMariaDBCustomerRepository {

    companion object {
        const val HOST = MariaDB.DEFAULT_HOST
        const val DATABASE = "db"
        const val USER = "user"
        const val PASSWORD = "password"
        const val TABLE = "customers"

        /**
         * A free port on the host, chosen anew at each run, so that parallel runs do not clash.
         * Notice: the port is released before MariaDB binds it, so another process may steal it in the meanwhile.
         */
        val PORT: Int = ServerSocket(0).use { it.localPort }

        private lateinit var composeFile: File

        /**
         * Copies the docker-compose.yml.template resource into a fresh temporary directory, as docker-compose.yml,
         * replacing each `__KEY__` placeholder with the corresponding value in [assignments].
         * The directory name is also the Docker Compose project name, hence each run gets its own
         * containers, networks, and volumes.
         */
        private fun createComposeFile(vararg assignments: Pair<String, String>): File {
            val directory = Files.createTempDirectory("docker-compose-testing").toFile()
            val path = directory.resolve("docker-compose.yml")
            val template = TestMariaDBCustomerRepository::class.java.getResource("docker-compose.yml.template")
                ?.readText()
                ?: error("docker-compose.yml.template resource not found")
            path.writeText(
                assignments.fold(template) { acc, (variable, value) -> acc.replace("__${variable}__", value) },
            )
            return path
        }

        /**
         * Calls `docker compose -f $composeFile $arguments`, waits for its completion,
         * and makes the test fail if the exit value is not 0.
         */
        private fun executeDockerCompose(vararg arguments: String) {
            val command = listOf("docker", "compose", "-f", composeFile.absolutePath) + arguments
            val exitValue = ProcessBuilder(command).inheritIO().start().waitFor()
            assertEquals(
                expected = 0,
                actual = exitValue,
                message = "Command `${command.joinToString(" ")}` returned non-zero exit code: $exitValue",
            )
        }

        @BeforeClass
        @JvmStatic
        fun setUpMariaDb() {
            composeFile = createComposeFile(
                "VERSION" to "11",
                "ROOT_PASSWORD" to UUID.randomUUID().toString(),
                "DB_NAME" to DATABASE,
                "USER" to USER,
                "PASSWORD" to PASSWORD,
                "PORT" to "$PORT",
            )
            executeDockerCompose("up", "--wait")
            executeDockerCompose("logs", "db", "--tail", "1")
        }

        @AfterClass
        @JvmStatic
        fun tearDownMariaDb() {
            executeDockerCompose("logs", "db")
            executeDockerCompose("down", "--volumes")
            composeFile.parentFile.deleteRecursively()
        }
    }

    private val connectionFactory = MariaDBConnectionFactory(DATABASE, USER, PASSWORD, HOST, PORT)
    private lateinit var repository: SqlCustomerRepository

    @Before
    fun createFreshTable() {
        repository = SqlCustomerRepository(connectionFactory, TABLE)
        repository.createTable(replaceIfPresent = true)
    }

    private val taxCode = TaxCode("CTTGNN92D07D468M")

    private val person = Customer.person(
        taxCode,
        "Giovanni",
        "Ciatto",
        LocalDate.of(1992, 4, 7),
    )

    private val person2 = person.clone(
        birthDate = LocalDate.of(1992, 4, 8),
        id = TaxCode("CTTGNN92D08D468M"),
    )

    private val vatNumber = VatNumber(12345678987L)

    private val company = Customer.company(
        vatNumber,
        "ACME",
        "Inc.",
        LocalDate.of(1920, 1, 1),
    )

    @Test
    fun complexTestWhichShouldActuallyBeDecomposedInSmallerTests() {
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(taxCode),
        )
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(vatNumber),
        )
        repository.add(person)
        repository.add(company)
        assertEquals(
            expected = listOf(person),
            actual = repository.findById(taxCode),
        )
        assertEquals(
            expected = listOf(company),
            actual = repository.findById(vatNumber),
        )
        repository.remove(vatNumber)
        repository.update(taxCode, person2)
        assertFailsWith<IllegalArgumentException>("Invalid customer id:") {
            repository.remove(taxCode)
        }
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(taxCode),
        )
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(vatNumber),
        )
        assertEquals(
            expected = listOf(person2),
            actual = repository.findById(person2.id),
        )
        listOf("Giovanni", "Ciatto").forEach {
            assertEquals(
                expected = listOf(person2),
                actual = repository.findByName(it),
            )
        }
    }
}
