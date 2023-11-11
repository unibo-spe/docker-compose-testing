package it.unibo.spe.compose.impl

import it.unibo.spe.compose.Customer
import it.unibo.spe.compose.CustomerID
import it.unibo.spe.compose.CustomerRepository
import it.unibo.spe.compose.db.ConnectionFactory
import java.lang.IllegalArgumentException
import java.sql.Connection
import java.sql.ResultSet

class SqlCustomerRepository(
    private val connections: ConnectionFactory,
    private val tableName: String,
) : CustomerRepository {

    fun createTable(replaceIfPresent: Boolean = false) =
        updating(
            query = "CREATE " +
                if (replaceIfPresent) { "OR REPLACE " } else { "" } +
                "TABLE $tableName (\n" +
                "    ${Customer::id.name} VARCHAR(16) PRIMARY KEY,\n" +
                "    ${Customer::firstName.name} VARCHAR(255),\n" +
                "    ${Customer::lastName.name} VARCHAR(255),\n" +
                "    ${Customer::birthDate.name} DATE\n" +
                ");",
            expectedResult = 0,
        )

    private fun <T> connecting(action: (Connection) -> T) = connections.connect().use(action)

    private fun <T> querying(query: String, action: (ResultSet) -> T): T =
        connecting { conn ->
            conn.createStatement().use {
                println(query)
                it.executeQuery(query).use(action)
            }
        }

    private fun <T> updating(query: String, action: (Int) -> T): T =
        connecting { conn ->
            conn.createStatement().use {
                println(query)
                it.executeUpdate(query).let(action)
            }
        }

    private fun updating(query: String, expectedResult: Int): Unit =
        updating(query) { require(it == expectedResult) }

    private fun ResultSet.retrieveCustomer(): Customer =
        Customer.of(
            id = CustomerID.parse(getString(Customer::id.name)),
            firstName = getString(Customer::firstName.name),
            lastName = getString(Customer::lastName.name),
            birthDate = getDate(Customer::birthDate.name).toLocalDate(),
        )

    private fun ResultSet.retrieveAllCostumers(): Iterable<Customer> =
        buildList {
            while (next()) {
                add(retrieveCustomer())
            }
        }

    override fun findById(id: CustomerID): Iterable<Customer> =
        querying("""SELECT * FROM $tableName WHERE ${Customer::id.name} = "${id.value}";""") {
            it.retrieveAllCostumers()
        }

    override fun findByName(name: String): Iterable<Customer> =
        querying(
            "SELECT * FROM $tableName WHERE " +
                """${Customer::firstName.name} LIKE "$name" OR """ +
                """${Customer::lastName.name} LIKE "$name";""",
        ) { it.retrieveAllCostumers() }

    private fun Customer.toTupleString() =
        """("${id.value}", "$firstName", "$lastName", "$birthDate")"""

    override fun add(customer: Customer) =
        updating(
            query = "INSERT INTO $tableName VALUES ${customer.toTupleString()};",
            expectedResult = 1,
        )

    override fun update(oldId: CustomerID, customer: Customer) {
        updating(
            query = "UPDATE $tableName SET\n" +
                "    ${Customer::id.name} = \"${customer.id.value}\",\n" +
                "    ${Customer::firstName.name} = \"${customer.firstName}\",\n" +
                "    ${Customer::lastName.name} = \"${customer.lastName}\",\n" +
                "    ${Customer::birthDate.name} = \"${customer.birthDate}\"\n" +
                """WHERE ${Customer::id.name} = "${oldId.value}";""",
            expectedResult = 1,
        )
    }

    override fun remove(id: CustomerID) {
        updating("""DELETE FROM $tableName WHERE ${Customer::id.name} = "${id.value}";""") {
            if (it != 1) {
                throw IllegalArgumentException("Invalid customer id: $id")
            }
        }
    }
}
