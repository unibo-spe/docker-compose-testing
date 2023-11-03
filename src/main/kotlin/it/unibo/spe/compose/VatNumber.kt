package it.unibo.spe.compose

data class VatNumber(override val value: Long) : CustomerID {
    init {
        require(value in 1..99999999999L)
    }
}
