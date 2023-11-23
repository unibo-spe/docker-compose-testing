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
        const val DATABASE = "db"
        const val USER = "user"
        const val PASSWORD = "password"
        const val TABLE = "customers"

        /**
         * The port on the host where MariaDB will be reachable.
         * Why not simply `MariaDB.DEFAULT_PORT` (i.e. 3306)?
         * Think about two runs of this test suite, in parallel, on the same machine.
         */
        val PORT: Int by lazy<Int> {
            TODO("find a free port on the host. Hint: open a java.net.ServerSocket on port 0, and read its localPort")
        }

        private lateinit var composeFile: File

        /**
         * Generates the actual Docker Compose file from the docker-compose.yml.template resource.
         *
         * Why a fresh temporary *directory* for each run, rather than a fixed path (or just a temporary file)?
         * Hint: how does Docker Compose choose the names of the containers, networks, and volumes it creates?
         */
        private fun createComposeFile(vararg assignments: Pair<String, String>): File {
            // TODO 1. create a fresh temporary directory (cf. java.nio.file.Files.createTempDirectory)
            // TODO 2. read the docker-compose.yml.template resource (cf. TestMariaDBCustomerRepository::class.java.getResource),
            //  failing loudly if it is missing
            // TODO 3. replace each `__KEY__` placeholder with the corresponding value, for each KEY in assignments
            // TODO 4. write the result into a docker-compose.yml file inside the temporary directory, and return that file
            TODO("generate the Compose file from the template, given ${assignments.size} assignments")
        }

        /**
         * Calls `docker compose -f $composeFile $arguments`, waits for its completion,
         * and makes the test fail if the exit value is not 0.
         */
        private fun executeDockerCompose(vararg arguments: String) {
            // TODO 1. build the command line: docker compose -f <absolute path of composeFile> <arguments>
            //  (-f is necessary, as composeFile is neither in the working directory, nor named compose.yaml)
            // TODO 2. run it via java.lang.ProcessBuilder (cf. inheritIO(), to see Docker's output in the test logs)
            // TODO 3. wait for the process to terminate
            // TODO 4. assert that the exit value is 0, with an informative message
            TODO("call docker compose ${arguments.joinToString(" ")}")
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
            // TODO start the stack, and return only when MariaDB is ready to accept connections
            //  (cf. `docker compose up --help`: which option blocks until services are running/healthy?)
            executeDockerCompose("TODO")
        }

        @AfterClass
        @JvmStatic
        fun tearDownMariaDb() {
            // TODO (optional) print the logs of the db service, useful to debug failures
            // TODO stop the stack, removing *everything* it created (cf. `docker compose down --help`: volumes too?)
            // TODO delete the temporary directory containing composeFile
            executeDockerCompose("TODO")
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
