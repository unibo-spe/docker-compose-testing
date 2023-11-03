package it.unibo.spe.compose.impl

import it.unibo.spe.compose.Customer
import it.unibo.spe.compose.CustomerID
import java.time.LocalDate

data class CustomerImpl(
    override val id: CustomerID,
    override val firstName: String,
    override val lastName: String,
    override val birthDate: LocalDate,
) : Customer
