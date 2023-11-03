package it.unibo.spe.compose

import it.unibo.spe.compose.db.MariaDB
import it.unibo.spe.compose.db.MariaDBConnectionFactory
import it.unibo.spe.compose.impl.SqlCustomerRepository
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class TestMariaDBCustomerRepository {

    companion object {
        const val HOST = MariaDB.DEFAULT_HOST
        const val PORT = MariaDB.DEFAULT_PORT
        const val DATABASE = "db"
        const val USER = "user"
        const val PASSWORD = "password"
        const val TABLE = "customers"

        @BeforeClass
        @JvmStatic
        fun setUpMariaDb() {
            // TODO: copy the "docker-compose.yml" resource into a temporary directory D
            // TODO: replace variables in __THIS_FORM__ in the copied file, to match the constants above
            // TODO: use docker compose command programmatically to start one new fresh MariaDB instance
            // TODO: wait for the MariaDB instance to be up and running
            TODO("implement me")
        }

        @AfterClass
        @JvmStatic
        fun tearDownMariaDb() {
            // TODO: use docker compose command programmatically to stop the currently running MariaDB instance
            TODO("implement me")
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
        repository.remove(taxCode)
        repository.remove(vatNumber)
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(taxCode),
        )
        assertEquals(
            expected = emptyList(),
            actual = repository.findById(vatNumber),
        )
    }
}
