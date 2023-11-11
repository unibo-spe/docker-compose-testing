package it.unibo.spe.compose

import it.unibo.spe.compose.db.MariaDB
import it.unibo.spe.compose.db.MariaDBConnectionFactory
import it.unibo.spe.compose.impl.SqlCustomerRepository
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import java.io.File
import java.lang.IllegalArgumentException
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestMariaDBCustomerRepository {

    companion object {
        const val HOST = MariaDB.DEFAULT_HOST
        const val PORT = MariaDB.DEFAULT_PORT
        const val DATABASE = "db"
        const val USER = "user"
        const val PASSWORD = "password"
        const val TABLE = "customers"

        private lateinit var composeFile: File

        private fun createComposeFile(path: File, vararg assignments: Pair<String, String>): File {
            val variablesAssignments = assignments.toMap()
            TestMariaDBCustomerRepository::class.java.getResource("docker-compose.yml")
                ?.openStream()?.bufferedReader()?.use { reader ->
                    path.bufferedWriter().use { writer ->
                        reader.forEachLine { line ->
                            val newLine = variablesAssignments.entries.fold(line) { acc, (variable, value) ->
                                acc.replace("__${variable}__", value)
                            }
                            writer.write(newLine)
                            writer.newLine()
                        }
                    }
                } ?: error("docker-compose.yaml resource not found")
            return path
        }

        private fun executeDockerCompose(vararg arguments: String, async: Boolean = false): Process {
            val command = mutableListOf("docker", "compose", "-f", composeFile.absolutePath)
            command.addAll(arguments)
            val commandString = command.joinToString(" ")
            return ProcessBuilder(command).inheritIO().start().also {
                if (!async) {
                    it.waitFor()
                    assertEquals(
                        expected = 0,
                        actual = it.exitValue(),
                        message = "Command `$commandString` returned non-zero exit code: ${it.exitValue()}",
                    )
                }
            }
        }

        @BeforeClass
        @JvmStatic
        fun setUpMariaDb() {
            composeFile = createComposeFile(
                path = File.createTempFile("docker-compose", ".yml"),
                "VERSION" to "latest",
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
