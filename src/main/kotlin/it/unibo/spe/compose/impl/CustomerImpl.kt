package it.unibo.spe.compose.impl

import it.unibo.spe.compose.Customer
import it.unibo.spe.compose.CustomerID
import java.time.LocalDate

data class CustomerImpl(
    override val id: CustomerID,
    override val firstName: String,
    override val lastName: String,
    override val birthDate: LocalDate,
) : Customer {
    override fun clone(id: CustomerID, firstName: String, lastName: String, birthDate: LocalDate): Customer =
        copy(id, firstName, lastName, birthDate)
}
