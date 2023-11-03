package it.unibo.spe.compose

import it.unibo.spe.compose.impl.CustomerImpl
import java.time.LocalDate

interface Customer {
    val id: CustomerID
    val firstName: String
    val lastName: String
    val birthDate: LocalDate
    val name: String
        get() = "$firstName $lastName"

    val isPerson: Boolean
        get() = id is TaxCode

    val isCompany: Boolean
        get() = id is VatNumber

    companion object {
        @JvmStatic
        fun person(code: TaxCode, firstName: String, lastName: String, birthDate: LocalDate): Customer =
            of(code, firstName, lastName, birthDate)

        @JvmStatic
        fun company(number: VatNumber, firstName: String, lastName: String, birthDate: LocalDate): Customer =
            of(number, firstName, lastName, birthDate)

        @JvmStatic
        fun of(id: CustomerID, firstName: String, lastName: String, birthDate: LocalDate): Customer {
            return CustomerImpl(id, firstName, lastName, birthDate)
        }
    }
}
